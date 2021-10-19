package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.enumerate.GlobalMessageType;
import com.github.dactiv.basic.socket.server.receiver.chat.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.chat.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.data.BasicMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.dactiv.basic.commons.Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 聊天业务逻辑服务
 *
 * @author maurice.chen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatService implements InitializingBean {

    /**
     * 聊天信息事件名称
     */
    public static final String CHAT_MESSAGE_EVENT_NAME = "chat_message";
    /**
     * 聊天信息读取事件名称
     */
    public static final String CHAT_READ_MESSAGE_EVENT_NAME = "chat_read_message";

    @Autowired
    private ChatConfig chatConfig;

    @Autowired
    private SocketServerManager socketServerManager;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private CipherAlgorithmService cipherAlgorithmService;

    @Autowired
    private RedissonClient redissonClient;

    @Getter
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 获取 redis 全局消息桶
     *
     * @param filename 文件名称
     *
     * @return redis 桶
     */
    private RBucket<GlobalMessage> getRedisGlobalMessageBucket(String filename, boolean global) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);

        CacheProperties cache = getGlobalMessageCacheProperties(global);

        return redissonClient.getBucket(cache.getName(key));
    }

    /**
     * 获取全局消息缓存配置
     *
     * @param global 是否全局消息，true 是，否则 用户消息
     *
     * @return 缓存配置
     */
    private CacheProperties getGlobalMessageCacheProperties(boolean global) {
        return global ? chatConfig.getGlobal().getCache() : chatConfig.getContact().getCache();
    }

    /**
     * 获取全局消息
     *
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @return 全局消息
     */
    public GlobalMessage getGlobalMessage(Integer sourceId, Integer targetId, boolean global) {
        String filename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);

        Bucket minioBucket = chatConfig.getContact().getContactBucket();

        if (global) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);

            filename = MessageFormat.format(chatConfig.getGlobal().getFileToken(), min, max);
            minioBucket = chatConfig.getGlobal().getBucket();
        }

        RBucket<GlobalMessage> redisBucket = getRedisGlobalMessageBucket(filename, global);

        GlobalMessage globalMessage = redisBucket.get();

        if (Objects.isNull(globalMessage)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            globalMessage = minioTemplate.readJsonValue(fileObject, GlobalMessage.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessage();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(minioBucket.getBucketName());
            globalMessage.setType(global ? GlobalMessageType.Global.getValue() : GlobalMessageType.Contact.getValue());
        }

        return globalMessage;
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
    private String getGlobalMessageCurrentFilename(GlobalMessage global, Integer sourceId, Integer targetId) throws Exception {
        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentMessageFile();
            Integer count = global.getMessageFileMap().get(filename);
            if (count + 1 > chatConfig.getMessage().getBatchSize()) {
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
    private String createHistoryMessageFile(GlobalMessage global, Integer sourceId, Integer targetId) throws Exception {
        String globalFilename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);

        if (GlobalMessageType.Global.getValue().equals(global.getType())) {
            globalFilename = MessageFormat.format(chatConfig.getGlobal().getFileToken(), sourceId, targetId);
        }

        String filename = createHistoryMessageFilename(globalFilename);
        global.setCurrentMessageFile(filename);
        global.getMessageFileMap().put(filename, 0);

        Bucket bucket = chatConfig.getContact().getContactBucket();

        if (GlobalMessageType.Global.getValue().equals(global.getType())) {
            bucket = chatConfig.getGlobal().getBucket();
        }

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        minioTemplate.writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        minioTemplate.writeJsonValue(messageFileObject, new LinkedList<BasicMessage.Message>());

        return filename;
    }

    /**
     * 创建历史消息文件名称
     *
     * @param filename 后缀文件名
     *
     * @return 新的历史消息文件名称
     */
    private String createHistoryMessageFilename(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String target = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        String suffix = target + WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + LocalDateTime.now().format(formatter);
        return MessageFormat.format(chatConfig.getMessage().getFileToken(), suffix);
    }

    /**
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global 是否全局消息（由系统保存的历史记录消息）
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessage.FileMessage> addHistoryMessage(List<GlobalMessage.Message> messages, Integer sourceId, Integer targetId, boolean global) throws Exception {

        GlobalMessage globalMessage = getGlobalMessage(sourceId, targetId, global);
        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);
        List<BasicMessage.FileMessage> fileMessageList = new LinkedList<>();
        for (GlobalMessage.Message message : messages) {

            String lastMessage = RegExUtils.replaceAll(
                    message.getContent(),
                    Constants.REPLACE_HTML_TAG_REX,
                    StringUtils.EMPTY
            );

            globalMessage.setLastMessage(lastMessage);
            globalMessage.setLastSendTime(new Date());

            BasicMessage.FileMessage fileMessage = BasicMessage.FileMessage.of(message, filename);
            CipherService cipherService = cipherAlgorithmService.getCipherService(fileMessage.getCryptoType());

            byte[] key = Base64.decode(fileMessage.getCryptoKey());
            byte[] plainText = fileMessage.getContent().getBytes(StandardCharsets.UTF_8);

            ByteSource cipherText = cipherService.encrypt(plainText, key);
            fileMessage.setContent(cipherText.getBase64());
            fileMessageList.add(fileMessage);
        }

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        List<GlobalMessage.Message> senderMessageList = minioTemplate.readJsonValue(
                messageFileObject,
                new TypeReference<>() {
                }
        );
        senderMessageList.addAll(fileMessageList);
        minioTemplate.writeJsonValue(messageFileObject, senderMessageList);

        globalMessage.getMessageFileMap().put(filename, senderMessageList.size());
        FileObject globalFileObject = FileObject.of(globalMessage.getBucketName(), globalMessage.getFilename());
        minioTemplate.writeJsonValue(globalFileObject, globalMessage);

        RBucket<GlobalMessage> bucket = getRedisGlobalMessageBucket(globalMessage.getFilename(), global);
        CacheProperties cache = getGlobalMessageCacheProperties(global);

        if (Objects.nonNull(cache.getExpiresTime())) {
            bucket.setAsync(globalMessage, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
        } else {
            bucket.setAsync(globalMessage);
        }

        if (Objects.nonNull(chatConfig.getContact().getCache().getExpiresTime())) {
            bucket.expire(
                    chatConfig.getContact().getCache().getExpiresTime().getValue(),
                    chatConfig.getContact().getCache().getExpiresTime().getUnit()
            );
        }

        return fileMessageList;
    }

    /**
     * 发送消息
     *
     * @param senderId    发送人用户 id
     * @param recipientId 收信人用户 id
     * @param content     消息内容
     */
    @SocketMessage
    @Concurrent(
            value = "socket:chat:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public GlobalMessage.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {

        GlobalMessage.Message message = new GlobalMessage.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(chatConfig.getCryptoType());
        message.setCryptoKey(chatConfig.getCryptoKey());

        List<BasicMessage.FileMessage> sourceMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                false
        );
        List<BasicMessage.FileMessage> targetMessages = addHistoryMessage(
                Collections.singletonList(message),
                recipientId,
                senderId,
                false
        );
        // 添加全局聊天记录文件
        List<BasicMessage.FileMessage> globalMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                true
        );

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(recipientId);

        ContactMessage contactMessage = new ContactMessage();

        String lastMessage = RegExUtils.replaceAll(
                message.getContent(),
                Constants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        contactMessage.setId(senderId);
        contactMessage.setTargetId(recipientId);
        contactMessage.setLastSendTime(new Date());
        contactMessage.setLastMessage(lastMessage);

        // 构造响应给目标用户的消息内容
        ContactMessage targetMessage = Casts.of(contactMessage, ContactMessage.class);
        List<BasicMessage.UserMessageBody> userMessageBodies = targetMessages
                .stream()
                .map(m -> this.createUserMessageBody(m, sourceMessages, globalMessages))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());
        targetMessage.getMessages().addAll(userMessageBodies);
        // 保存未读记录
        addUnreadMessage(recipientId, targetMessage);

        // 如果当前用户在线，推送消息到客户端
        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {
            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_MESSAGE_EVENT_NAME,
                    targetMessage
            );
        }

        contactMessage.getMessages().add(message);

        amqpTemplate.convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveMessageReceiver.DEFAULT_QUEUE_NAME,
                contactMessage
        );

        return message;
    }

    private BasicMessage.UserMessageBody createUserMessageBody(BasicMessage.FileMessage m,
                                                               List<BasicMessage.FileMessage> sourceMessages,
                                                               List<BasicMessage.FileMessage> globalMessages) {

        BasicMessage.UserMessageBody result = Casts.of(m, BasicMessage.UserMessageBody.class);
        result.getFilenames().add(m.getFilename());

        BasicMessage.FileMessage sourceUserMessage = sourceMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + m.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(sourceUserMessage.getFilename());

        BasicMessage.FileMessage globalMessage = globalMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + m.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(globalMessage.getFilename());

        return result;
    }

    /**
     * 添加未读消息
     *
     * @param userId 用户 id
     * @param contactMessage 联系人消息
     */
    private void addUnreadMessage(Integer userId, ContactMessage contactMessage) throws Exception {
        String filename = MessageFormat.format(chatConfig.getContact().getUnreadMessageFileToken(), userId);
        FileObject fileObject = FileObject.of(chatConfig.getContact().getUnreadBucket(), filename);
        Map<Integer, ContactMessage> map = minioTemplate.readJsonValue(fileObject, new TypeReference<>(){});

        if (MapUtils.isEmpty(map)) {
            map = new LinkedHashMap<>();
        }

        ContactMessage targetMessage = map.get(userId);

        if (Objects.isNull(targetMessage)) {
            map.put(userId, contactMessage);
        }  else {
            targetMessage.getMessages().addAll(contactMessage.getMessages());
        }


        minioTemplate.writeJsonValue(fileObject, map);
    }

    /**
     * 添加常用联系人
     *
     * @param userId 用户 id
     * @param contactId 联系人 id
     */
    public void addRecentContact(Integer userId, Integer contactId) throws Exception {
        String filename = MessageFormat.format(chatConfig.getContact().getRecentFileToken(), userId);
        FileObject fileObject = FileObject.of(chatConfig.getContact().getRecentBucket(), filename);
        List<IntegerIdEntity> idEntities = minioTemplate.readJsonValue(fileObject, new TypeReference<>(){});

        if (CollectionUtils.isEmpty(idEntities)) {
            idEntities = new LinkedList<>();
        }

        IntegerIdEntity idEntity = idEntities
                .stream()
                .filter(i -> i.getId().equals(contactId))
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(idEntity)) {
            idEntity.setCreationTime(new Date());
        } else {
            idEntity = new IntegerIdEntity();
            idEntity.setId(contactId);
            idEntities.add(idEntity);
        }

        for (int i = 0; i < idEntities.size() - chatConfig.getContact().getRecentCount(); i++) {

            Optional<IntegerIdEntity> optional = idEntities
                    .stream()
                    .min(Comparator.comparing(IntegerIdEntity::getCreationTime));

            if (optional.isEmpty()) {
                break;
            }

            idEntities.removeIf(entity -> entity.getId().equals(optional.get().getId()));
        }

        minioTemplate.writeJsonValue(fileObject, idEntities);
    }

    /**
     * 读取信息
     *
     * @param body 读取消息 request body
     */
    @SocketMessage
    public void readMessage(ReadMessageRequestBody body) {

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(body.getSenderId());

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, body.getRecipientId(),
                            GlobalMessage.DEFAULT_MESSAGE_IDS, body.getMessages().values()
                    )
            );
        }

        amqpTemplate.convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                ReadMessageReceiver.DEFAULT_QUEUE_NAME,
                body
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        minioTemplate.makeBucketIfNotExists(chatConfig.getGlobal().getBucket());
        minioTemplate.makeBucketIfNotExists(chatConfig.getMessage().getBucket());
        minioTemplate.makeBucketIfNotExists(chatConfig.getContact().getContactBucket());
        minioTemplate.makeBucketIfNotExists(chatConfig.getContact().getUnreadBucket());
        minioTemplate.makeBucketIfNotExists(chatConfig.getContact().getRecentBucket());
    }
}
