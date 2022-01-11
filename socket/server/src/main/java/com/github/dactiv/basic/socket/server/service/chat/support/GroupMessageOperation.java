package com.github.dactiv.basic.socket.server.service.chat.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.domain.BroadcastMessage;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.MessageDeleteRecord;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.SaveGroupTempMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.SaveRecentContactReceiver;
import com.github.dactiv.basic.socket.server.service.RoomParticipantService;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.AbstractMessageOperation;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
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
 * 群聊聊天信息的消息操作实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class GroupMessageOperation extends AbstractMessageOperation<BasicMessageMeta.GroupReadableMessage> {


    /**
     * 已读数量
     */
    public static final String READ_COUNT_FIELD = "readCount";

    private final RoomParticipantService roomParticipantService;

    public GroupMessageOperation(ChatConfig chatConfig,
                                 MinioTemplate minioTemplate,
                                 SocketServerManager socketServerManager,
                                 AmqpTemplate amqpTemplate,
                                 CipherAlgorithmService cipherAlgorithmService,
                                 RedissonClient redissonClient,
                                 RoomParticipantService roomParticipantService) {
        super(chatConfig, minioTemplate, socketServerManager, amqpTemplate, cipherAlgorithmService, redissonClient);
        this.roomParticipantService = roomParticipantService;
    }

    @Override
    public boolean isSupport(MessageTypeEnum type) {
        return MessageTypeEnum.GROUP.equals(type);
    }

    @Override
    public GlobalMessagePage getHistoryMessagePage(Integer userId,
                                                   Integer targetId,
                                                   Date time,
                                                   ScrollPageRequest pageRequest) {
        GlobalMessageMeta globalMessage = getGlobalMessage(targetId);

        GlobalMessagePage messagePage = getGlobalMessagePage(
                globalMessage,
                time,
                pageRequest
        );

        MessageDeleteRecord dto = getGroupMessageDeleteRecord(userId, targetId);

        List<BasicMessageMeta.FileMessage> filterElements = messagePage
                .getElements()
                .stream()
                .filter(m -> !dto.getDeleteMap().getOrDefault(m.getFilename(), new LinkedList<>()).contains(m.getId()))
                .collect(Collectors.toList());

        messagePage.setElements(filterElements);

        return messagePage;
    }

    @Override
    public List<Date> getHistoryMessageDateList(Integer userId, Integer targetId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        GlobalMessageMeta globalMessage = getGlobalMessage(targetId);

        MessageDeleteRecord dto = getGroupMessageDeleteRecord(userId, targetId);

        globalMessage
                .getMessageFileMap()
                .entrySet()
                .removeIf(p -> dto.getDeleteMap().getOrDefault(p.getKey(), new LinkedList<>()).size() == p.getValue());

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
    public void readMessage(ReadMessageRequestBody body) {

        BroadcastMessage<Map<String, Object>> message = BroadcastMessage.of(
                body.getTargetId().toString(),
                CHAT_READ_MESSAGE_EVENT_NAME,
                Map.of(
                        IdEntity.ID_FIELD_NAME, body.getReaderId(),
                        ContactMessage.TARGET_ID_FIELD, body.getTargetId(),
                        NumberIdEntity.CREATION_TIME_FIELD_NAME, body.getCreationTime(),
                        IdentityNamingStrategy.TYPE_KEY, MessageTypeEnum.GROUP.getValue(),
                        GlobalMessageMeta.DEFAULT_MESSAGE_IDS, body.getMessageIds()
                )
        );

        SocketResultHolder.get().addBroadcastSocketMessage(message);

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveGroupTempMessageReceiver.DEFAULT_QUEUE_NAME,
                message
        );

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                ReadMessageReceiver.DEFAULT_QUEUE_NAME,
                body
        );
    }

    @Override
    protected Integer getUnreadMessageDataTargetId(ReadMessageRequestBody body) {
        return body.getTargetId();
    }

    @Override
    protected Integer getUnreadMessageMapDataTargetId(ContactMessage<BasicMessageMeta.FileLinkMessage> contactMessage) {
        return contactMessage.getTargetId();
    }

    @Override
    protected void postReadMessage(Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> unreadMessageData,
                                   List<BasicMessageMeta.FileLinkMessage> fileLinkMessages,
                                   ReadMessageRequestBody body) throws Exception {
        Long count = roomParticipantService.countByRoomId(body.getTargetId());

        if (Objects.isNull(count) || count <= 0) {
            return;
        }

        ContactMessage<BasicMessageMeta.FileLinkMessage> message = unreadMessageData.get(body.getTargetId());
        message.getMessages().removeIf(m -> isAllRead(fileLinkMessages, m, count));

        if (CollectionUtils.isEmpty(message.getMessages())) {
            unreadMessageData.remove(body.getTargetId());
        }

        String filename = MessageFormat.format(
                getChatConfig().getGlobal().getUnreadMessageFileToken(),
                MessageTypeEnum.GROUP.toString(),
                body.getReaderId()
        );

        FileObject fileObject = FileObject.of(getChatConfig().getGlobal().getUnreadBucket(), filename);
        getMinioTemplate().writeJsonValue(fileObject, unreadMessageData);
    }

    private boolean isAllRead(List<BasicMessageMeta.FileLinkMessage> fileLinkMessages,
                              BasicMessageMeta.FileLinkMessage message,
                              Long count) {
        return fileLinkMessages
                .stream()
                .anyMatch(umb -> umb.getId().equals(message.getId()) && umb.getPayload().get(READ_COUNT_FIELD).equals(count));
    }

    @Override
    protected void doReadMessage(FileObject messageFileObject,
                                 BasicMessageMeta.FileLinkMessage fileLinkMessage,
                                 ReadMessageRequestBody body) throws Exception {

        List<BasicMessageMeta.GroupReadableMessage> messageList = getMinioTemplate().readJsonValue(
                messageFileObject,
                new TypeReference<>() {
                }
        );

        Optional<BasicMessageMeta.GroupReadableMessage> messageOptional = messageList
                .stream()
                .filter(m -> m.getId().equals(fileLinkMessage.getId()))
                .findFirst();

        if (messageOptional.isPresent()) {
            BasicMessageMeta.GroupReadableMessage fileMessage = messageOptional.get();
            fileMessage.getReaderInfo().add(IntegerIdEntity.of(body.getReaderId(), body.getCreationTime()));
            fileLinkMessage.getPayload().put(READ_COUNT_FIELD, fileMessage.getReaderInfo().size());
        }

        getMinioTemplate().writeJsonValue(messageFileObject, messageList);
    }


    @Override
    @SocketMessage(value = SystemConstants.CHAT_FILTER_RESULT_ID, ignoreOtherIds = true)
    @Concurrent(
            value = "socket:chat:group:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        BasicMessageMeta.Message basic = createMessage(senderId, content, MessageTypeEnum.GROUP);
        BasicMessageMeta.GroupReadableMessage message = Casts.of(basic, BasicMessageMeta.GroupReadableMessage.class);
        // 添加全局聊天记录文件
        List<BasicMessageMeta.GroupReadableMessage> globalMessages = addHistoryMessage(
                List.of(Casts.of(message, BasicMessageMeta.GroupReadableMessage.class)),
                recipientId
        );

        ContactMessage<BasicMessageMeta.Message> contactMessage = createContactMessage(
                message,
                senderId,
                recipientId,
                MessageTypeEnum.GROUP
        );

        // 构造消息关联文件内容，用于已读时能够更改所有文件的状态为已读
        //noinspection unchecked
        ContactMessage<BasicMessageMeta.FileLinkMessage> recipientMessage = Casts.of(
                contactMessage,
                ContactMessage.class
        );

        // 通过 globalMessages 构造要发送的消息对应的多个文件
        List<BasicMessageMeta.FileLinkMessage> fileLinkMessages = globalMessages
                .stream()
                .map(m -> this.createFileLinkMessage(m, new LinkedList<>()))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());
        // 由于 ContactMessage 类的 messages 字段是 new 出来的，copy bean 会将对象引用到字段中，
        // 而下面由于调用了 contactMessage.getMessages().add(message); 就会产生这个 list 有两条 message 记录，
        // 所以在这里直接对一个新的集合给 recipientMessage 隔离开来添加数据
        recipientMessage.setMessages(fileLinkMessages);
        // 保存未读记录
        addUnreadMessage(recipientId, MessageTypeEnum.GROUP, recipientMessage);

        BroadcastMessage<ContactMessage<BasicMessageMeta.Message>> tempMessage = BroadcastMessage.of(
                recipientId.toString(),
                CHAT_MESSAGE_EVENT_NAME,
                RestResult.ofSuccess(contactMessage)
        );

        SocketResultHolder.get().addBroadcastSocketMessage(tempMessage);

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveGroupTempMessageReceiver.DEFAULT_QUEUE_NAME,
                tempMessage
        );

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveRecentContactReceiver.DEFAULT_QUEUE_NAME,
                contactMessage
        );

        return globalMessages.iterator().next();
    }

    /**
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param targetId 目标 id(收信者用户 id)
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessageMeta.GroupReadableMessage> addHistoryMessage(List<GlobalMessageMeta.GroupReadableMessage> messages,
                                                                         Integer targetId) throws Exception {

        GlobalMessageMeta globalMessage = getGlobalMessage(targetId);
        String filename = getGlobalMessageCurrentFilename(globalMessage, targetId);
        List<BasicMessageMeta.GroupReadableMessage> fileMessageList = saveMessages(messages, globalMessage, filename);

        RBucket<GlobalMessageMeta> bucket = getRedisGlobalMessageBucket(globalMessage.getFilename());
        CacheProperties cache = getChatConfig().getGroup().getGroupCache();

        if (Objects.nonNull(cache.getExpiresTime())) {
            bucket.setAsync(globalMessage, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
        } else {
            bucket.setAsync(globalMessage);
        }

        return fileMessageList;
    }

    /**
     * 获取全局消息当前索引存储消息的文件名称
     *
     * @param global   全局消息
     * @param targetId 目标 id(收信者用户 id)
     *
     * @return 文件名称
     *
     * @throws Exception 获取错误时抛出
     */
    private String getGlobalMessageCurrentFilename(GlobalMessageMeta global, Integer targetId) throws Exception {
        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentMessageFile();
            LocalDate before = getFileLocalDate(global);
            Integer count = global.getMessageFileMap().get(filename);
            LocalDate now = LocalDate.now();

            if (count > getChatConfig().getMessage().getBatchSize() || ChronoUnit.DAYS.between(before, now) >= 1) {
                filename = createHistoryMessageFile(global, targetId);
            }
        } else {
            filename = createHistoryMessageFile(global, targetId);
        }

        return filename;
    }

    /**
     * 创建历史消息文件
     *
     * @param global   全局消息
     * @param targetId 目标 id(收信者用户 id)
     *
     * @return 文件名称
     *
     * @throws Exception 创建文件失败时抛出
     */
    private String createHistoryMessageFile(GlobalMessageMeta global, Integer targetId) throws Exception {
        String globalFilename = MessageFormat.format(getChatConfig().getGlobal().getGroupFileToken(), targetId);
        Integer historyFileCount = getChatConfig().getContact().getHistoryMessageFileCount();
        Bucket bucket = getChatConfig().getGlobal().getBucket();
        return setGlobalMessageCurrentFilename(global, globalFilename, historyFileCount, bucket);
    }

    /**
     * 获取群聊已删除的记录
     *
     * @param userId 用户 id
     * @param roomId 房间 id
     *
     * @return 群聊消息删除记录
     */
    public MessageDeleteRecord getGroupMessageDeleteRecord(Integer userId, Integer roomId) {

        String filename = MessageFormat.format(getChatConfig().getGroup().getDeleteRecordFileToken(), userId, roomId);

        RBucket<MessageDeleteRecord> redisBucket = getRedisGroupMessageDeleteRecordBucket(filename);

        MessageDeleteRecord record = redisBucket.get();

        Bucket minioBucket = getChatConfig().getGroup().getDeleteMessageRecordBucket();
        if (Objects.isNull(record)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            record = getMinioTemplate().readJsonValue(fileObject, MessageDeleteRecord.class);
        }

        if (Objects.isNull(record)) {
            record = MessageDeleteRecord.of(userId, roomId);
        }

        return record;
    }

    /**
     * 从 redis 中获取群聊已删除的记录
     *
     * @param filename 群聊已删除的记录文件名
     *
     * @return redis 桶信息
     */
    private RBucket<MessageDeleteRecord> getRedisGroupMessageDeleteRecordBucket(String filename) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);

        CacheProperties cache = getChatConfig().getGroup().getDeleteRecordCache();

        return getRedissonClient().getBucket(cache.getName(key));
    }

    /**
     * 获取全局消息
     *
     * @param targetId 目标 id (收信者用户 id)
     *
     * @return 全局消息
     */
    public GlobalMessageMeta getGlobalMessage(Integer targetId) {

        String filename = MessageFormat.format(getChatConfig().getGlobal().getGroupFileToken(), targetId);

        RBucket<GlobalMessageMeta> redisBucket = getRedisGlobalMessageBucket(filename);

        GlobalMessageMeta globalMessage = redisBucket.get();

        Bucket minioBucket = getChatConfig().getGlobal().getBucket();
        if (Objects.isNull(globalMessage)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            globalMessage = getMinioTemplate().readJsonValue(fileObject, GlobalMessageMeta.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessageMeta();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(minioBucket.getBucketName());
            globalMessage.setType(MessageTypeEnum.GROUP);
        }

        return globalMessage;
    }

    /**
     * 获取 redis 全局消息桶
     *
     * @param filename 文件名称
     *
     * @return redis 桶
     */
    private RBucket<GlobalMessageMeta> getRedisGlobalMessageBucket(String filename) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);

        CacheProperties cache = getChatConfig().getGroup().getGroupCache();

        return getRedissonClient().getBucket(cache.getName(key));
    }

    @Override
    protected void install() throws Exception {
        super.install();
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getGroup().getDeleteMessageRecordBucket());
    }
}
