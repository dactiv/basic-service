package com.github.dactiv.basic.socket.server.service.chat.resolver.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.RecentContactMeta;
import com.github.dactiv.basic.socket.server.enumerate.ContactTypeEnum;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.basic.socket.server.service.chat.resolver.AbstractMessageResolver;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.FileObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.dactiv.basic.commons.SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 人员聊天信息的消息解析实现
 *
 * @author maurice.chen
 */
@Component
public class PersonMessageResolver extends AbstractMessageResolver implements InitializingBean {

    public PersonMessageResolver(ChatConfig chatConfig, MinioTemplate minioTemplate, SocketServerManager socketServerManager, AmqpTemplate amqpTemplate, CipherAlgorithmService cipherAlgorithmService, RedissonClient redissonClient) {
        super(chatConfig, minioTemplate, socketServerManager, amqpTemplate, cipherAlgorithmService, redissonClient);
    }

    @Override
    public boolean isSupport(MessageTypeEnum type) {
        return MessageTypeEnum.PERSON.equals(type);
    }

    @Override
    public GlobalMessagePage getHistoryMessagePage(Integer userId, Integer targetId, Date time, ScrollPageRequest pageRequest) {
        GlobalMessageMeta globalMessage = getGlobalMessage(userId, targetId, false);

        List<GlobalMessageMeta.FileMessage> messages = new LinkedList<>();
        LocalDateTime dateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
        List<String> historyFiles = globalMessage
                .getMessageFileMap()
                .keySet()
                .stream()
                .filter(s -> this.isHistoryMessageFileBeforeCurrentTime(s, dateTime))
                .sorted(Comparator.comparing(this::getHistoryFileCreationTime).reversed())
                .collect(Collectors.toList());

        for (String file : historyFiles) {
            FileObject fileObject = FileObject.of(getChatConfig().getMessage().getBucket(), file);
            List<GlobalMessageMeta.FileMessage> fileMessageList = getMinioTemplate().readJsonValue(
                    fileObject,
                    new TypeReference<>() {
                    }
            );

            List<GlobalMessageMeta.FileMessage> temps = fileMessageList
                    .stream()
                    .filter(f -> f.getCreationTime().before(time))
                    .sorted(Comparator.comparing(BasicMessageMeta.Message::getCreationTime).reversed())
                    .limit(pageRequest.getSize() - messages.size())
                    .peek(this::decryptMessageContent)
                    .collect(Collectors.toList());

            messages.addAll(temps);

            if (messages.size() >= pageRequest.getSize()) {
                break;
            }

        }

        GlobalMessagePage result = GlobalMessagePage.of(pageRequest, messages);

        result.setLastMessage(globalMessage.getLastMessage());
        result.setLastSendTime(globalMessage.getLastSendTime());

        return result;
    }

    @Override
    public List<Date> getHistoryMessageDateList(Integer userId, Integer targetId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        GlobalMessageMeta globalMessage = getGlobalMessage(userId, targetId, false);
        return  globalMessage
                .getMessageFileMap()
                .keySet()
                .stream()
                .map(this::getHistoryFileCreationTime)
                .map(k -> LocalDateTime.parse(k, formatter))
                .map(ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());
    }

    @Override
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    public void readMessage(Integer senderId, Integer recipientId, List<String> messageIds) throws Exception {
        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(senderId);

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    ChatService.CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, recipientId,
                            IdentityNamingStrategy.TYPE_KEY, ContactTypeEnum.PERSON.getValue(),
                            GlobalMessageMeta.DEFAULT_MESSAGE_IDS, messageIds
                    )
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    senderId,
                    ChatService.CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, recipientId,
                            GlobalMessageMeta.DEFAULT_MESSAGE_IDS, messageIds
                    )
            );
        }
    }

    /**
     * 获取未读消息数据
     *
     * @param userId 用户 id
     *
     * @return 未读消息数据
     */
    public Map<Integer, ContactMessage<BasicMessageMeta.UserMessageBody>> getUnreadMessageData(Integer userId) {
        String filename = MessageFormat.format(getChatConfig().getContact().getUnreadMessageFileToken(), userId);
        FileObject fileObject = FileObject.of(getChatConfig().getContact().getUnreadBucket(), filename);
        Map<Integer, ContactMessage<BasicMessageMeta.UserMessageBody>> map = getMinioTemplate().readJsonValue(
                fileObject,
                new TypeReference<>() {
                }
        );

        if (MapUtils.isEmpty(map)) {
            map = new LinkedHashMap<>();
        }

        return map;
    }

    @Override
    public void consumeReadMessage(Integer senderId, Integer recipientId, List<String> messageIds, Date readTime) throws Exception {
        Map<Integer, ContactMessage<BasicMessageMeta.UserMessageBody>> map = getUnreadMessageData(recipientId);

        if (MapUtils.isEmpty(map)) {
            return ;
        }

        ContactMessage<BasicMessageMeta.UserMessageBody> message = map.get(senderId);

        if (Objects.isNull(message)) {
            return ;
        }

        List<BasicMessageMeta.UserMessageBody> userMessageBodies = message
                .getMessages()
                .stream()
                .filter(umb -> messageIds.contains(umb.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userMessageBodies)) {
            return;
        }

        for (BasicMessageMeta.UserMessageBody userMessageBody : userMessageBodies) {

            for (String filename : userMessageBody.getFilenames()) {

                FileObject messageFileObject = FileObject.of(
                        getChatConfig().getMessage().getBucket(),
                        filename
                );

                List<BasicMessageMeta.FileMessage> messageList = getMinioTemplate().readJsonValue(
                        messageFileObject,
                        new TypeReference<>() {
                        }
                );

                Optional<BasicMessageMeta.FileMessage> messageOptional = messageList
                        .stream()
                        .filter(m -> m.getId().equals(userMessageBody.getId()))
                        .findFirst();

                if (messageOptional.isPresent()) {
                    BasicMessageMeta.FileMessage fileMessage = messageOptional.get();
                    fileMessage.setRead(true);
                    fileMessage.setReadTime(readTime);
                }

                getMinioTemplate().writeJsonValue(messageFileObject, messageList);
            }
        }
        message.getMessages().removeIf(m -> userMessageBodies.stream().anyMatch(umb -> umb.getId().equals(m.getId())));

        if (CollectionUtils.isEmpty(message.getMessages())) {
            map.remove(senderId);
        }

        String filename = MessageFormat.format(
                getChatConfig().getContact().getUnreadMessageFileToken(),
                recipientId
        );

        FileObject fileObject = FileObject.of(getChatConfig().getContact().getUnreadBucket(), filename);
        getMinioTemplate().writeJsonValue(fileObject, map);
    }

    @Override
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        GlobalMessageMeta.Message message = new GlobalMessageMeta.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(getChatConfig().getCryptoType());
        message.setCryptoKey(getChatConfig().getCryptoKey());
        message.setType(MessageTypeEnum.PERSON);

        List<BasicMessageMeta.FileMessage> sourceUserMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                false
        );
        List<BasicMessageMeta.FileMessage> targetUserMessages = addHistoryMessage(
                Collections.singletonList(message),
                recipientId,
                senderId,
                false
        );
        // 添加全局聊天记录文件
        List<BasicMessageMeta.FileMessage> globalMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                true
        );

        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(recipientId);

        ContactMessage<BasicMessageMeta.Message> contactMessage = new ContactMessage<>();

        String lastMessage = RegExUtils.replaceAll(
                message.getContent(),
                SystemConstants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        contactMessage.setId(senderId);
        contactMessage.setType(ContactTypeEnum.PERSON);
        contactMessage.setTargetId(recipientId);
        contactMessage.setLastSendTime(new Date());
        contactMessage.setLastMessage(lastMessage);
        contactMessage.getMessages().add(message);

        List<BasicMessageMeta.UserMessageBody> userMessageBodies = targetUserMessages
                .stream()
                .map(m -> this.createUserMessageBody(m, sourceUserMessages, globalMessages))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());

        // 构造未读消息内容，用于已读时能够更改所有文件的状态为已读
        //noinspection unchecked
        ContactMessage<BasicMessageMeta.UserMessageBody> recipientMessage = Casts.of(contactMessage, ContactMessage.class);
        // 由于 ContactMessage 类的 messages 字段是 new 出来的，copy bean 会注解将对象引用到字段中，
        // 而下面由调用了 contactMessage.getMessages().add(message); 就会产生这个 list 由两条 message记录，
        // 所以在这里直接对一个新的集合给 recipientMessage 隔离开来添加数据
        recipientMessage.setMessages(userMessageBodies);

        //noinspection unchecked
        ContactMessage<BasicMessageMeta.FileMessage> unicastMessage = Casts.of(contactMessage, ContactMessage.class);
        unicastMessage.setMessages(targetUserMessages);
        unicastMessage.getMessages().forEach(this::decryptMessageContent);

        // 如果当前用户在线，推送消息到客户端
        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {
            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    ChatService.CHAT_MESSAGE_EVENT_NAME,
                    unicastMessage
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    recipientId,
                    ChatService.CHAT_MESSAGE_EVENT_NAME,
                    unicastMessage
            );
        }

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveMessageReceiver.DEFAULT_QUEUE_NAME,
                contactMessage
        );

        return sourceUserMessages.iterator().next();
    }

    /**
     * 创建用户消息体
     *
     * @param message        消息实体
     * @param sourceMessages 来源消息集合
     * @param globalMessages 全局消息集合
     *
     * @return 用户消息体
     */
    private BasicMessageMeta.UserMessageBody createUserMessageBody(BasicMessageMeta.FileMessage message,
                                                                   List<BasicMessageMeta.FileMessage> sourceMessages,
                                                                   List<BasicMessageMeta.FileMessage> globalMessages) {

        BasicMessageMeta.UserMessageBody result = Casts.of(message, BasicMessageMeta.UserMessageBody.class);
        result.getFilenames().add(message.getFilename());

        BasicMessageMeta.FileMessage sourceUserMessage = sourceMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + message.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(sourceUserMessage.getFilename());

        BasicMessageMeta.FileMessage globalMessage = globalMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + message.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(globalMessage.getFilename());

        return result;
    }

    /**
     * 判断历史消息文件是否小于指定时间
     *
     * @param filename 文件名称
     * @param time 时间
     *
     * @return true 是，否则 false
     */
    private boolean isHistoryMessageFileBeforeCurrentTime(String filename, LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);
        LocalDateTime creationTime = LocalDateTime.parse(text, formatter);
        return creationTime.isBefore(time);
    }

    /**
     * 获取消息历史文件创建时间
     *
     * @param filename 文件名称
     *
     * @return 创建时间戳
     */
    private String getHistoryFileCreationTime(String filename) {
        return StringUtils.substringBefore(
                StringUtils.substringAfterLast(filename, "_"),
                ".json"
        );
    }



    /**
     * 获取常用联系人 id 集合
     *
     * @param userId 用户 id
     *
     * @return 常用联系人 id 集合
     */
    public List<RecentContactMeta> getRecentContacts(Integer userId) {
        List<RecentContactMeta> idEntities = getRecentContactData(userId);

        return idEntities
                .stream()
                .sorted(Comparator.comparing(IntegerIdEntity::getCreationTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取常用联系人数据
     *
     * @param userId 用户 id
     *
     * @return 常用联系人数据集合
     */
    public List<RecentContactMeta> getRecentContactData(Integer userId) {
        String filename = MessageFormat.format(getChatConfig().getContact().getRecentFileToken(), userId);
        FileObject fileObject = FileObject.of(getChatConfig().getContact().getRecentBucket(), filename);
        List<RecentContactMeta> recentContacts = getMinioTemplate().readJsonValue(fileObject, new TypeReference<>() {});

        if (CollectionUtils.isEmpty(recentContacts)) {
            recentContacts = new LinkedList<>();
        }

        return recentContacts;
    }

    /**
     * 获取常用联系人文件对象
     *
     * @param userId 用户 id
     *
     * @return 常用联系人文件对象
     */
    public FileObject getRecentContactFileObject(Integer userId) {
        String filename = MessageFormat.format(getChatConfig().getContact().getRecentFileToken(), userId);
        return FileObject.of(getChatConfig().getContact().getRecentBucket(), filename);
    }

    /**
     * 添加常用联系人
     *
     * @param userId    用户 id
     * @param contactId 联系人 id
     * @param type 联系人类型
     */
    public void addRecentContact(Integer userId, Integer contactId, ContactTypeEnum type) throws Exception {
        List<RecentContactMeta> recentContacts = getRecentContactData(userId);

        RecentContactMeta recentContact = recentContacts
                .stream()
                .filter(i -> i.getId().equals(contactId) && i.getType().equals(type))
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(recentContact)) {
            recentContact.setCreationTime(new Date());
        } else {
            recentContact = new RecentContactMeta();
            recentContact.setId(contactId);
            recentContact.setType(type);
            recentContacts.add(recentContact);
        }

        for (int i = 0; i < recentContacts.size() - getChatConfig().getContact().getRecentCount(); i++) {

            Optional<RecentContactMeta> optional = recentContacts
                    .stream()
                    .min(Comparator.comparing(IntegerIdEntity::getCreationTime));

            if (optional.isEmpty()) {
                break;
            }

            recentContacts.removeIf(entity -> entity.getId().equals(optional.get().getId()));
        }
        FileObject fileObject = getRecentContactFileObject(userId);
        getMinioTemplate().writeJsonValue(fileObject, recentContacts);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getGlobal().getBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getMessage().getBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getContact().getContactBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getContact().getUnreadBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getContact().getRecentBucket());
    }
}
