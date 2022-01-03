package com.github.dactiv.basic.socket.server.service.chat.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.RecentContactMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.AbstractMessageOperation;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.CodecUtils;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.dactiv.basic.commons.SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 人员聊天信息的消息操作实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class PersonMessageOperation extends AbstractMessageOperation implements InitializingBean {

    public PersonMessageOperation(ChatConfig chatConfig,
                                  MinioTemplate minioTemplate,
                                  SocketServerManager socketServerManager,
                                  AmqpTemplate amqpTemplate,
                                  CipherAlgorithmService cipherAlgorithmService,
                                  RedissonClient redissonClient) {
        super(chatConfig, minioTemplate, socketServerManager, amqpTemplate, cipherAlgorithmService, redissonClient);
    }

    @Override
    public boolean isSupport(MessageTypeEnum type) {
        return MessageTypeEnum.CONTACT.equals(type);
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
    public void readMessage(ReadMessageRequestBody body) throws Exception {
        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(body.getSenderId());

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, body.getRecipientId(),
                            IdentityNamingStrategy.TYPE_KEY, MessageTypeEnum.CONTACT.getValue(),
                            GlobalMessageMeta.DEFAULT_MESSAGE_IDS, body.getMessageIds()
                    )
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    body.getSenderId(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, body.getRecipientId(),
                            GlobalMessageMeta.DEFAULT_MESSAGE_IDS, body.getMessageIds()
                    )
            );
        }

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                ReadMessageReceiver.DEFAULT_QUEUE_NAME,
                body
        );
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
    public void consumeReadMessage(ReadMessageRequestBody body) throws Exception {
        Map<Integer, ContactMessage<BasicMessageMeta.UserMessageBody>> map = getUnreadMessageData(body.getRecipientId());

        if (MapUtils.isEmpty(map)) {
            return ;
        }

        ContactMessage<BasicMessageMeta.UserMessageBody> message = map.get(body.getSenderId());

        if (Objects.isNull(message)) {
            return ;
        }

        List<BasicMessageMeta.UserMessageBody> userMessageBodies = message
                .getMessages()
                .stream()
                .filter(umb -> body.getMessageIds().contains(umb.getId()))
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
                    fileMessage.setReadTime(body.getCreationTime());
                }

                getMinioTemplate().writeJsonValue(messageFileObject, messageList);
            }
        }
        message.getMessages().removeIf(m -> userMessageBodies.stream().anyMatch(umb -> umb.getId().equals(m.getId())));

        if (CollectionUtils.isEmpty(message.getMessages())) {
            map.remove(body.getSenderId());
        }

        String filename = MessageFormat.format(
                getChatConfig().getContact().getUnreadMessageFileToken(),
                body.getRecipientId()
        );

        FileObject fileObject = FileObject.of(getChatConfig().getContact().getUnreadBucket(), filename);
        getMinioTemplate().writeJsonValue(fileObject, map);
    }

    @Override
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @Concurrent(
            value = "socket:chat:person:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        GlobalMessageMeta.Message message = createMessage(senderId, content, MessageTypeEnum.CONTACT);

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
        contactMessage.setType(MessageTypeEnum.CONTACT);
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
                    CHAT_MESSAGE_EVENT_NAME,
                    unicastMessage
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    recipientId,
                    CHAT_MESSAGE_EVENT_NAME,
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
    public void addRecentContact(Integer userId, Integer contactId, MessageTypeEnum type) throws Exception {
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



    /**
     * 解密消息内容
     *
     * @param message 文件消息
     */
    protected void decryptMessageContent(BasicMessageMeta.FileMessage message) {
        CipherService cipherService = getCipherAlgorithmService().getCipherService(message.getCryptoType());
        String content = message.getContent();
        String key = message.getCryptoKey();

        byte[] bytes = cipherService.decrypt(com.github.dactiv.framework.crypto.algorithm.Base64.decode(content), com.github.dactiv.framework.crypto.algorithm.Base64.decode(key)).obtainBytes();

        try {
            message.setContent(new String(bytes, CodecUtils.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            String msg = "对内容为 [" + content + "] 的消息通过密钥 [" +
                    key + "] 使用 [" + message.getCryptoType() + "] 解密失败";
            log.warn(msg, e);
        }
    }

    /**
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessageMeta.FileMessage> addHistoryMessage(List<GlobalMessageMeta.Message> messages,
                                                                Integer sourceId,
                                                                Integer targetId,
                                                                boolean global) throws Exception {

        GlobalMessageMeta globalMessage = getGlobalMessage(sourceId, targetId, global);
        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);
        List<BasicMessageMeta.FileMessage> fileMessageList = saveMessages(messages, globalMessage, filename);

        RBucket<GlobalMessageMeta> bucket = getRedisGlobalMessageBucket(globalMessage.getFilename(), global);
        CacheProperties cache = getGlobalMessageCacheProperties(global);

        if (Objects.nonNull(cache.getExpiresTime())) {
            bucket.setAsync(globalMessage, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
        } else {
            bucket.setAsync(globalMessage);
        }

        if (Objects.nonNull(getChatConfig().getContact().getCache().getExpiresTime())) {
            bucket.expire(
                    getChatConfig().getContact().getCache().getExpiresTime().getValue(),
                    getChatConfig().getContact().getCache().getExpiresTime().getUnit()
            );
        }

        return fileMessageList;
    }

    /**
     * 获取全局消息
     *
     * @param sourceId 来源 id (发送者用户 id)
     * @param targetId 目标 id (收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @return 全局消息
     */
    public GlobalMessageMeta getGlobalMessage(Integer sourceId, Integer targetId, boolean global) {

        String filename = MessageFormat.format(getChatConfig().getContact().getFileToken(), sourceId, targetId);

        Bucket minioBucket = getChatConfig().getContact().getContactBucket();

        if (global) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);

            filename = MessageFormat.format(getChatConfig().getGlobal().getPersonFileToken(), min, max);
            minioBucket = getChatConfig().getGlobal().getBucket();
        }

        RBucket<GlobalMessageMeta> redisBucket = getRedisGlobalMessageBucket(filename, global);

        GlobalMessageMeta globalMessage = redisBucket.get();

        if (Objects.isNull(globalMessage)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            globalMessage = getMinioTemplate().readJsonValue(fileObject, GlobalMessageMeta.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessageMeta();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(minioBucket.getBucketName());
            globalMessage.setType(global ? MessageTypeEnum.GLOBAL : MessageTypeEnum.CONTACT);
        }

        return globalMessage;
    }

    /**
     * 获取 redis 全局消息桶
     *
     * @param filename 文件名称
     * @param global 是否获取全局文件的通
     *
     * @return redis 桶
     */
    private RBucket<GlobalMessageMeta> getRedisGlobalMessageBucket(String filename, boolean global) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);

        CacheProperties cache = getGlobalMessageCacheProperties(global);

        return getRedissonClient().getBucket(cache.getName(key));
    }

    /**
     * 获取全局消息缓存配置
     *
     * @param global 是否全局消息，true 是，否则 用户消息
     *
     * @return 缓存配置
     */
    private CacheProperties getGlobalMessageCacheProperties(boolean global) {
        return global ? getChatConfig().getGlobal().getCache() : getChatConfig().getContact().getCache();
    }

    /**
     * 获取全局消息当前索引存储消息的文件名称
     *
     * @param global   全局消息
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     *
     * @return 文件名称
     *
     * @throws Exception 获取错误时抛出
     */
    private String getGlobalMessageCurrentFilename(GlobalMessageMeta global, Integer sourceId, Integer targetId) throws Exception {
        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentMessageFile();

            LocalDate before = getFileLocalDate(global);
            Integer count = global.getMessageFileMap().get(filename);
            LocalDate now = LocalDate.now();

            if (count > getChatConfig().getMessage().getBatchSize() || ChronoUnit.DAYS.between(before, now) >= 1) {
                filename = createHistoryMessageFile(global, sourceId, targetId);
            }
        } else {
            filename = createHistoryMessageFile(global, sourceId, targetId);
        }

        return filename;
    }

    /**
     * 创建历史消息文件
     *
     * @param global   全局消息
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     *
     * @return 文件名称
     *
     * @throws Exception 创建文件失败时抛出
     */
    private String createHistoryMessageFile(GlobalMessageMeta global, Integer sourceId, Integer targetId) throws Exception {
        String globalFilename = MessageFormat.format(getChatConfig().getContact().getFileToken(), sourceId, targetId);
        Integer historyFileCount = getChatConfig().getContact().getHistoryMessageFileCount();

        if (MessageTypeEnum.GLOBAL.equals(global.getType())) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);
            globalFilename = MessageFormat.format(getChatConfig().getGlobal().getPersonFileToken(), min, max);
            historyFileCount = getChatConfig().getGlobal().getHistoryMessageFileCount();
        }

        setGlobalMessageCurrentFilename(global, globalFilename, historyFileCount);

        Bucket bucket = getChatConfig().getContact().getContactBucket();

        if (MessageTypeEnum.GLOBAL.equals(global.getType())) {
            bucket = getChatConfig().getGlobal().getBucket();
        }

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        getMinioTemplate().writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(getChatConfig().getMessage().getBucket(), global.getFilename());
        getMinioTemplate().writeJsonValue(messageFileObject, new LinkedList<BasicMessageMeta.Message>());

        return global.getFilename();
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
