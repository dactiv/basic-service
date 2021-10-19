package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.receiver.chat.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.chat.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.data.BasicMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.FileObject;
import org.apache.commons.collections.MapUtils;
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

    @Autowired
    private MinioTemplate minioTemplate;

    private RBucket<GlobalMessage> getGlobalMessageBucket(String filename) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        return redissonClient.getBucket(chatConfig.getContact().getCache().getName(key));
    }

    private GlobalMessage getGlobalMessage(Integer sourceId, Integer targetId, boolean global) throws Exception {
        String filename = MessageFormat.format(chatConfig.getContact().getContactFileToken(), sourceId, targetId);

        if (global) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);
            filename = MessageFormat.format(chatConfig.getGlobal().getFileToken(), min, max);
        }

        RBucket<GlobalMessage> bucket = getGlobalMessageBucket(filename);

        GlobalMessage globalMessage = bucket.get();

        if (Objects.isNull(globalMessage)) {
            FileObject senderGlobalFile = FileObject.of(chatConfig.getContact().getBucket(), filename);
            globalMessage = minioTemplate.readJsonValue(senderGlobalFile, GlobalMessage.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessage();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(chatConfig.getContact().getBucket().getBucketName());

            //FileObject senderGlobalFile = FileObject.of(chatConfig.getContact().getBucket(), filename);
            //minioTemplate.writeJsonValue(senderGlobalFile, globalMessage);
        }

        /*bucket.setAsync(globalMessage);
        if (Objects.nonNull(chatConfig.getContact().getCache().getExpiresTime())) {
            bucket.expire(
                    chatConfig.getContact().getCache().getExpiresTime().getValue(),
                    chatConfig.getContact().getCache().getExpiresTime().getUnit()
            );
        }*/

        return globalMessage;
    }

    private String getGlobalMessageCurrentFilename(GlobalMessage global, Integer senderId, Integer recipientId) throws Exception {
        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentMessageFile();
            Integer count = global.getMessageFileMap().get(filename);
            if (count + 1 > chatConfig.getMessage().getBatchSize()) {
                filename = createHistoryMessageFile(global, senderId, recipientId);
            }
        } else {
            filename = createHistoryMessageFile(global, senderId, recipientId);
        }

        return filename;
    }

    private String createHistoryMessageFile(GlobalMessage global, Integer sourceId, Integer targetId) throws Exception {
        String globalFilename = MessageFormat.format(chatConfig.getContact().getContactFileToken(), sourceId, targetId);

        String filename = createMessageFilename(globalFilename);
        global.setCurrentMessageFile(filename);
        global.getMessageFileMap().put(filename, 0);

        FileObject globalFileObject = FileObject.of(chatConfig.getContact().getBucket(), globalFilename);
        minioTemplate.writeJsonValue(globalFileObject, global);
        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        minioTemplate.writeJsonValue(messageFileObject, new LinkedList<BasicMessage.Message>());

        return filename;
    }

    private void addHistoryMessage(GlobalMessage.Message message, Integer sourceId, Integer targetId, boolean global) throws Exception {
        GlobalMessage globalMessage = getGlobalMessage(sourceId, targetId, global);

        String lastMessage = RegExUtils.replaceAll(
                message.getContent(),
                Constants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        globalMessage.setLastMessage(lastMessage);
        globalMessage.setLastSendTime(new Date());

        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);

        BasicMessage.UserMessage userMessage = BasicMessage.UserMessage.of(message, filename);
        CipherService cipherService = cipherAlgorithmService.getCipherService(userMessage.getCryptoType());

        byte[] key = Base64.decode(userMessage.getCryptoKey());
        byte[] plainText = userMessage.getContent().getBytes(StandardCharsets.UTF_8);

        ByteSource cipherText = cipherService.encrypt(plainText, key);
        userMessage.setContent(cipherText.getBase64());

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        List<GlobalMessage.Message> senderMessageList = minioTemplate.readJsonValue(
                messageFileObject,
                new TypeReference<>() {}
        );
        senderMessageList.add(userMessage);
        minioTemplate.writeJsonValue(messageFileObject, senderMessageList);

        FileObject globalFileObject = FileObject.of(globalMessage.getBucketName(),globalMessage.getFilename());
        minioTemplate.writeJsonValue(globalFileObject, globalMessage);

        RBucket<GlobalMessage> bucket = getGlobalMessageBucket(globalMessage.getFilename());
        bucket.setAsync(globalMessage);
        if (Objects.nonNull(chatConfig.getContact().getCache().getExpiresTime())) {
            bucket.expire(
                    chatConfig.getContact().getCache().getExpiresTime().getValue(),
                    chatConfig.getContact().getCache().getExpiresTime().getUnit()
            );
        }
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

        addHistoryMessage(message, senderId, recipientId, false);
        addHistoryMessage(message, recipientId, senderId, false);

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

        contactMessage.getMessages().add(message);

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(recipientId);

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {
            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_MESSAGE_EVENT_NAME,
                    contactMessage
            );
        }

        amqpTemplate.convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveMessageReceiver.DEFAULT_QUEUE_NAME,
                contactMessage
        );

        return message;
    }

    /**
     * 保存联系人信息
     *
     * @param contactMessage 联系人信息
     */
    public void saveContactMessage(ContactMessage contactMessage) throws Exception {
        for (BasicMessage.Message message : contactMessage.getMessages()) {
            addHistoryMessage(message, contactMessage.getId(), contactMessage.getTargetId(), true);
        }

    }

    private String createMessageFilename(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String target = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        String suffix = target + WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + LocalDateTime.now().format(formatter);
        return MessageFormat.format(chatConfig.getMessage().getFileToken(), suffix);
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
        minioTemplate.makeBucketIfNotExists(chatConfig.getContact().getBucket());
    }
}
