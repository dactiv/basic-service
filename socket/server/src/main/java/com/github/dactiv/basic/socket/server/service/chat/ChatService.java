package com.github.dactiv.basic.socket.server.service.chat;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.commons.minio.MinioUtils;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.receiver.chat.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.chat.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;

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

    /**
     * 发送消息
     *
     * @param senderId    发送人用户 id
     * @param recipientId 收信人用户 id
     * @param content     消息内容
     */
    @SocketMessage
    @Concurrent(
            value = "socket:chat:send:[T(java.lang.Math.min(#senderId, #recipientId))]_[T(java.lang.Math.max(#senderId, #recipientId))]",
            exception = "请不要过快的发送消息"
    )
    public GlobalMessage.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        String senderFile = MessageFormat.format(chatConfig.getMessage().getContactFileToken(), senderId, recipientId);

        GlobalMessage global = MinioUtils.readJsonValue(
                chatConfig.getMessage().getBucketName(),
                senderFile,
                GlobalMessage.class
        );

        if (Objects.isNull(global)) {
            global = new GlobalMessage();
        }

        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentFile();
            Integer count = global.getMessageFileMap().get(filename);
            if (count + 1 > chatConfig.getMessage().getBatchSize()) {
                String globalFilename = MessageFormat.format(chatConfig.getMessage().getContactFileToken(), senderId, recipientId);
                filename = createMessageFilename(globalFilename);

                global.setCurrentFile(filename);
                global.getMessageFileMap().put(filename, 0);

                MinioUtils.writeJsonValue(chatConfig.getMessage().getBucketName(), globalFilename, global);
            }
        } else {
            String globalFilename = MessageFormat.format(chatConfig.getMessage().getContactFileToken(), senderId, recipientId);

            filename = createMessageFilename(globalFilename);
            global.setCurrentFile(filename);
            global.getMessageFileMap().put(filename, 0);
        }

        GlobalMessage.Message message = new GlobalMessage.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(chatConfig.getCryptoType());
        message.setCryptoKey(chatConfig.getCryptoKey());
        message.setFilename(filename);

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
    public void saveContactMessage(ContactMessage contactMessage) {

        /*List<SaveObject<GlobalMessage>> saveObjects = createGlobalMessageMap(contactMessage);

        Map<String, List<GlobalMessage.Message>> messageListMap = new LinkedHashMap<>();

        for (SaveObject<GlobalMessage> o : saveObjects) {

            Map.Entry<String, List<GlobalMessage.Message>> messagesMap = createGlobalTargetMessages(
                    o.getFilename(),
                    o.getObject()
            );

            for (BasicMessage.Message message : contactMessage.getMessages()) {

                if (messagesMap.getValue().size() >= chatConfig.getMessage().getBatchSize()) {

                    messagesMap = createGlobalTargetMessages(
                            o.getFilename(),
                            o.getObject()
                    );

                    o.getObject().getMessageFiles().add(messagesMap.getKey());

                    messageListMap.put(messagesMap.getKey(), messagesMap.getValue());
                }

                BasicMessage.Message cipherMessage = Casts.of(message, BasicMessage.Message.class);

                CipherService cipherService = cipherAlgorithmService.getCipherService(cipherMessage.getCryptoType());

                byte[] key = Base64.decode(cipherMessage.getCryptoKey());
                byte[] plainText = cipherMessage.getContent().getBytes(StandardCharsets.UTF_8);

                ByteSource cipherText = cipherService.encrypt(plainText, key);
                cipherMessage.setContent(cipherText.getBase64());

                messagesMap.getValue().add(cipherMessage);
            }

        }*/

    }

    /*private Map.Entry<String, List<GlobalMessage.Message>> createGlobalTargetMessages(String filename, GlobalMessage globalMessage) {
        String messageFilename;

        if (CollectionUtils.isEmpty(globalMessage.getMessageFiles())) {
            messageFilename = createMessageFilename(filename);
            globalMessage.getMessageFiles().add(messageFilename);
        } else {
            messageFilename = globalMessage.getMessageFiles().iterator().next();
        }

        List<GlobalMessage.Message> messages = MinioUtils.readJsonValue(
                chatConfig.getMessage().getBucketName(),
                messageFilename,
                new TypeReference<>() {
                }
        );

        if (CollectionUtils.isEmpty(messages)) {
            messages = new LinkedList<>();
        }

        return Map.of(messageFilename, messages).entrySet().iterator().next();
    }*/

    private String createMessageFilename(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String target = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        String suffix = target + WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + LocalDateTime.now().format(formatter);

        return MessageFormat.format(chatConfig.getMessage().getContactFileToken(), suffix);
    }

    /*private List<SaveObject<GlobalMessage>> createGlobalMessageMap(ContactMessage message) {
        List<SaveObject<GlobalMessage>> result = new LinkedList<>();

        Integer min = Math.min(message.getId(), message.getTargetId());
        Integer max = Math.max(message.getId(), message.getTargetId());

        String globalFile = MessageFormat.format(chatConfig.getGlobal().getFileToken(), min, max);
        GlobalMessage globalMessage = createGlobalMessageIfNotExist(chatConfig.getGlobal().getBucketName(), globalFile);
        result.add(SaveObject.of(chatConfig.getGlobal().getBucketName(), globalFile, globalMessage));

        ChatConfig.Message config = chatConfig.getMessage();
        String contactFileToken = chatConfig.getMessage().getContactFileToken();

        String senderFile = MessageFormat.format(contactFileToken, message.getId(), message.getTargetId());
        GlobalMessage senderMessage = createGlobalMessageIfNotExist(config.getBucketName(), senderFile);
        result.add(SaveObject.of(config.getBucketName(), senderFile, senderMessage));

        String targetFile = MessageFormat.format(contactFileToken, message.getTargetId(), message.getId());
        GlobalMessage targetMessage = createGlobalMessageIfNotExist(config.getBucketName(), targetFile);
        result.add(SaveObject.of(config.getBucketName(), targetFile, targetMessage));

        return result;
    }

    private GlobalMessage createGlobalMessageIfNotExist(String bucketName, String filename) {
        GlobalMessage globalMessage = MinioUtils.readJsonValue(
                bucketName,
                filename,
                GlobalMessage.class
        );

        return Objects.isNull(globalMessage) ? new GlobalMessage() : globalMessage;
    }*/

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
        MinioUtils.makeBucketIfNotExists(chatConfig.getGlobal().getBucketName());
        MinioUtils.makeBucketIfNotExists(chatConfig.getMessage().getBucketName());
    }
}
