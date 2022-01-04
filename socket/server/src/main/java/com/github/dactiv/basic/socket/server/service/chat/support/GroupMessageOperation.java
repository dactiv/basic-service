package com.github.dactiv.basic.socket.server.service.chat.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.domain.BroadcastMessage;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.MessageDeleteRecord;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.SaveGroupTempMessageReceiver;
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
public class GroupMessageOperation extends AbstractMessageOperation {

    public GroupMessageOperation(ChatConfig chatConfig,
                                 MinioTemplate minioTemplate,
                                 SocketServerManager socketServerManager,
                                 AmqpTemplate amqpTemplate,
                                 CipherAlgorithmService cipherAlgorithmService,
                                 RedissonClient redissonClient) {
        super(chatConfig, minioTemplate, socketServerManager, amqpTemplate, cipherAlgorithmService, redissonClient);
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

        GlobalMessagePage messagePage = getGlobalMessagePage(globalMessage, time, pageRequest);
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
    public void readMessage(ReadMessageRequestBody body) {

        List<String> messageIds = body
                .getMessageMap()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        BroadcastMessage<Map<String, Object>> message = BroadcastMessage.of(
                body.getTargetId().toString(),
                CHAT_READ_MESSAGE_EVENT_NAME,
                Map.of(
                        IdEntity.ID_FIELD_NAME, body.getReaderId(),
                        ContactMessage.TARGET_ID_FIELD, body.getTargetId(),
                        NumberIdEntity.CREATION_TIME_FIELD_NAME, body.getCreationTime(),
                        IdentityNamingStrategy.TYPE_KEY, MessageTypeEnum.GROUP.getValue(),
                        GlobalMessageMeta.DEFAULT_MESSAGE_IDS, messageIds
                )
        );

        SocketResultHolder.get().addBroadcastSocketMessage(message);

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveGroupTempMessageReceiver.DEFAULT_QUEUE_NAME,
                message
        );
    }

    @Override
    public void consumeReadMessage(ReadMessageRequestBody body) throws Exception {

        List<FileObject> fileObjects = body
                .getMessageMap()
                .keySet()
                .stream()
                .map(filename -> FileObject.of(getChatConfig().getMessage().getBucket(), filename))
                .collect(Collectors.toList());

        for (FileObject messageFileObject : fileObjects) {

            List<BasicMessageMeta.FileMessage> messageList = getMinioTemplate().readJsonValue(
                    messageFileObject,
                    new TypeReference<>() {
                    }
            );

            List<String> ids = body.getMessageMap().get(messageFileObject.getObjectName());

            Optional<BasicMessageMeta.FileMessage> messageOptional = messageList
                    .stream()
                    .filter(m -> ids.contains(m.getId()))
                    .findFirst();

            if (messageOptional.isPresent()) {
                BasicMessageMeta.GroupReadableMessage fileMessage = Casts.cast(
                        messageOptional.get(),
                        BasicMessageMeta.GroupReadableMessage.class
                );
                IntegerIdEntity idEntity = new IntegerIdEntity();

                idEntity.setId(body.getTargetId());
                idEntity.setCreationTime(body.getCreationTime());

                fileMessage.getReaderInfo().add(idEntity);
            }

            getMinioTemplate().writeJsonValue(messageFileObject, messageList);
        }
    }

    @Override
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @Concurrent(
            value = "socket:chat:group:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        BasicMessageMeta.Message message = createMessage(senderId, content, MessageTypeEnum.GROUP);
        // 添加全局聊天记录文件
        List<BasicMessageMeta.FileMessage> globalMessages = addHistoryMessage(
                Collections.singletonList(message),
                recipientId
        );

        ContactMessage<BasicMessageMeta.Message> contactMessage = createContactMessage(
                message,
                senderId,
                recipientId,
                MessageTypeEnum.GROUP
        );
        // FIXME 这里有点啰嗦，为什么要转型成 BasicMessageMeta.FileMessage 而不是在 createContactMessage 直接返回 createContactMessage 类型 ？
        //noinspection unchecked
        ContactMessage<BasicMessageMeta.FileMessage> broadcastMessage = Casts.of(contactMessage, ContactMessage.class);
        broadcastMessage.setMessages(globalMessages);
        broadcastMessage.getMessages().forEach(this::decryptMessageContent);

        BroadcastMessage<GlobalMessageMeta.Message> tempMessage = BroadcastMessage.of(
                recipientId.toString(),
                CHAT_MESSAGE_EVENT_NAME,
                RestResult.ofSuccess(message)
        );

        SocketResultHolder.get().addBroadcastSocketMessage(tempMessage);

        getAmqpTemplate().convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveGroupTempMessageReceiver.DEFAULT_QUEUE_NAME,
                tempMessage
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
    public List<BasicMessageMeta.FileMessage> addHistoryMessage(List<GlobalMessageMeta.Message> messages,
                                                                Integer targetId) throws Exception {

        GlobalMessageMeta globalMessage = getGlobalMessage(targetId);
        String filename = getGlobalMessageCurrentFilename(globalMessage, targetId);
        List<BasicMessageMeta.FileMessage> fileMessageList = saveMessages(messages, globalMessage, filename);

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
        String globalFilename = MessageFormat.format(getChatConfig().getGroup().getFileToken(), targetId);
        Integer historyFileCount = getChatConfig().getContact().getHistoryMessageFileCount();
        Bucket bucket = getChatConfig().getGlobal().getBucket();
        return setGlobalMessageCurrentFilename(global, globalFilename, historyFileCount, bucket);
    }

    public MessageDeleteRecord getGroupMessageDeleteRecord(Integer userId, Integer roomId) {

        String filename = MessageFormat.format(getChatConfig().getGroup().getDeleteRecordFileToken(), userId, roomId);

        RBucket<MessageDeleteRecord> redisBucket = getRedisGroupMessageDeleteRecordBucket(filename);

        MessageDeleteRecord dto = redisBucket.get();

        Bucket minioBucket = getChatConfig().getGroup().getDeleteMessageRecordBucket();
        if (Objects.isNull(dto)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            dto = getMinioTemplate().readJsonValue(fileObject, MessageDeleteRecord.class);
        }

        if (Objects.isNull(dto)) {
            dto = MessageDeleteRecord.of(userId, roomId);
        }

        return dto;
    }

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

        String filename = MessageFormat.format(getChatConfig().getGroup().getFileToken(), targetId);

        RBucket<GlobalMessageMeta> redisBucket = getRedisGlobalMessageBucket(filename);

        GlobalMessageMeta globalMessage = redisBucket.get();

        Bucket minioBucket = getChatConfig().getGroup().getGroupBucket();
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
}
