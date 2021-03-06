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
import com.github.dactiv.basic.socket.server.receiver.SaveRecentContactReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.AbstractMessageOperation;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.stereotype.Component;

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
public class PersonMessageOperation extends AbstractMessageOperation<BasicMessageMeta.ContactReadableMessage> {

    public PersonMessageOperation(ChatConfig chatConfig,
                                  MinioTemplate minioTemplate,
                                  SocketServerManager socketServerManager,
                                  AmqpTemplate amqpTemplate,
                                  CipherAlgorithmService cipherAlgorithmService,
                                  RedissonClient redissonClient) {
        super(chatConfig, minioTemplate, socketServerManager, amqpTemplate, cipherAlgorithmService, redissonClient);
    }

    @Override
    protected void postReadMessage(Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> unreadMessageData,
                                   List<BasicMessageMeta.FileLinkMessage> fileLinkMessages,
                                   ReadMessageRequestBody body) throws Exception {

        ContactMessage<BasicMessageMeta.FileLinkMessage> message = unreadMessageData.get(body.getTargetId());
        message.getMessages().removeIf(m -> fileLinkMessages.stream().anyMatch(umb -> umb.getId().equals(m.getId())));

        if (CollectionUtils.isEmpty(message.getMessages())) {
            unreadMessageData.remove(body.getTargetId());
        }

        String filename = MessageFormat.format(
                getChatConfig().getGlobal().getUnreadMessageFileToken(),
                MessageTypeEnum.CONTACT.toString(),
                body.getReaderId()
        );

        FileObject fileObject = FileObject.of(getChatConfig().getGlobal().getUnreadBucket(), filename);
        getMinioTemplate().writeJsonValue(fileObject, unreadMessageData);
    }

    @Override
    protected void doReadMessage(FileObject messageFileObject,
                                 BasicMessageMeta.FileLinkMessage fileLinkMessage,
                                 ReadMessageRequestBody body) throws Exception {
        List<BasicMessageMeta.ContactReadableMessage> messageList = getMinioTemplate().readJsonValue(
                messageFileObject,
                new TypeReference<>() {
                }
        );

        Optional<BasicMessageMeta.ContactReadableMessage> messageOptional = messageList
                .stream()
                .filter(m -> m.getId().equals(fileLinkMessage.getId()))
                .findFirst();

        if (messageOptional.isPresent()) {
            BasicMessageMeta.ContactReadableMessage fileMessage = messageOptional.get();
            fileMessage.setRead(true);
            fileMessage.setReadTime(body.getCreationTime());
        }

        getMinioTemplate().writeJsonValue(messageFileObject, messageList);
    }

    @Override
    public boolean isSupport(MessageTypeEnum type) {
        return MessageTypeEnum.CONTACT.equals(type);
    }

    @Override
    public GlobalMessagePage getHistoryMessagePage(Integer userId, Integer targetId, Date time, ScrollPageRequest pageRequest) {
        GlobalMessageMeta globalMessage = getGlobalMessage(userId, targetId, false);
        return getGlobalMessagePage(globalMessage, time, pageRequest);
    }

    @Override
    public List<Date> getHistoryMessageDateList(Integer userId, Integer targetId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        GlobalMessageMeta globalMessage = getGlobalMessage(userId, targetId, false);

        return globalMessage
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
        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(body.getTargetId());

        Map<String, Object> message = Map.of(
                IdEntity.ID_FIELD_NAME, body.getReaderId(),
                NumberIdEntity.CREATION_TIME_FIELD_NAME, body.getCreationTime(),
                IdentityNamingStrategy.TYPE_KEY, MessageTypeEnum.CONTACT.getValue(),
                GlobalMessageMeta.DEFAULT_MESSAGE_IDS, body.getMessageIds()
        );

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {
            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    message
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    body.getTargetId(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    message
            );
        }

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                ReadMessageReceiver.DEFAULT_QUEUE_NAME,
                body
        );
    }

    @Override
    @SocketMessage(value = SystemConstants.CHAT_FILTER_RESULT_ID, ignoreOtherIds = true)
    @Concurrent(
            value = "socket:chat:person:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {

        BasicMessageMeta.Message basic = createMessage(senderId, content, MessageTypeEnum.CONTACT);
        BasicMessageMeta.ContactReadableMessage message = Casts.of(basic, BasicMessageMeta.ContactReadableMessage.class);

        List<BasicMessageMeta.ContactReadableMessage> sourceUserMessages = addHistoryMessage(
                List.of(Casts.of(message, BasicMessageMeta.ContactReadableMessage.class)),
                senderId,
                recipientId,
                false
        );
        List<BasicMessageMeta.ContactReadableMessage> targetUserMessages = addHistoryMessage(
                List.of(Casts.of(message, BasicMessageMeta.ContactReadableMessage.class)),
                recipientId,
                senderId,
                false
        );
        // 添加全局聊天记录文件
        List<BasicMessageMeta.ContactReadableMessage> globalMessages = addHistoryMessage(
                List.of(Casts.of(message, BasicMessageMeta.ContactReadableMessage.class)),
                senderId,
                recipientId,
                true
        );

        ContactMessage<BasicMessageMeta.Message> contactMessage = createContactMessage(
                message,
                senderId,
                recipientId,
                MessageTypeEnum.CONTACT
        );
        // 构造消息关联文件内容，用于已读时能够更改所有文件的状态为已读
        //noinspection unchecked
        ContactMessage<BasicMessageMeta.FileLinkMessage> recipientMessage = Casts.of(
                contactMessage,
                ContactMessage.class
        );

        List<BasicMessageMeta.ContactReadableMessage> sourceMessages = new ArrayList<>(List.copyOf(sourceUserMessages));
        sourceMessages.addAll(List.copyOf(globalMessages));
        // 通过 sourceUserMessages 和 globalMessages 构造消息对应出指定多个文件
        List<BasicMessageMeta.FileLinkMessage> fileLinkMessages = targetUserMessages
                .stream()
                .map(m -> this.createFileLinkMessage(m, sourceMessages))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());
        // 由于 ContactMessage 类的 messages 字段是 new 出来的，copy bean 会将对象引用到字段中，
        // 而下面由于调用了 contactMessage.getMessages().add(message); 就会产生这个 list 有两条 message 记录，
        // 所以在这里直接对一个新的集合给 recipientMessage 隔离开来添加数据
        recipientMessage.setMessages(fileLinkMessages);
        // 保存未读记录
        addUnreadMessage(recipientId, MessageTypeEnum.CONTACT, recipientMessage);

        //noinspection unchecked
        ContactMessage<BasicMessageMeta.ContactReadableMessage> unicastMessage = Casts.of(contactMessage, ContactMessage.class);
        unicastMessage.setMessages(targetUserMessages);
        unicastMessage.getMessages().forEach(this::decryptMessageContent);

        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(recipientId);
        // 如果当前用户在线，推送消息到客户端
        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {
            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_MESSAGE_EVENT_NAME,
                    contactMessage
            );
        } else {
            getSocketServerManager().saveTempMessage(
                    recipientId,
                    CHAT_MESSAGE_EVENT_NAME,
                    contactMessage
            );
        }

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveRecentContactReceiver.DEFAULT_QUEUE_NAME,
                contactMessage
        );

        return sourceUserMessages.iterator().next();
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
        List<RecentContactMeta> recentContacts = getMinioTemplate().readJsonValue(fileObject, new TypeReference<>() {
        });

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
     * @param type      联系人类型
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
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessageMeta.ContactReadableMessage> addHistoryMessage(List<BasicMessageMeta.ContactReadableMessage> messages,
                                                                           Integer sourceId,
                                                                           Integer targetId,
                                                                           boolean global) throws Exception {

        GlobalMessageMeta globalMessage = getGlobalMessage(sourceId, targetId, global);
        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);
        List<BasicMessageMeta.ContactReadableMessage> fileMessageList = saveMessages(messages, globalMessage, filename);

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
     * @param global   是否获取全局文件的通
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

        Bucket bucket = getChatConfig().getContact().getContactBucket();
        return setGlobalMessageCurrentFilename(global, globalFilename, historyFileCount, bucket);


    }

    @Override
    public void install() throws Exception {
        super.install();
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getContact().getContactBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getContact().getRecentBucket());
    }
}
