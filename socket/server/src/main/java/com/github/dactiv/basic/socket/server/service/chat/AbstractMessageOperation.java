package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.CodecUtils;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.WebDataBinder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象的消息操作实现，用于分离一下可复用的公共方法
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class AbstractMessageOperation implements MessageOperation, InitializingBean {

    @Getter
    private final ChatConfig chatConfig;

    @Getter
    private final MinioTemplate minioTemplate;

    @Getter
    private final SocketServerManager socketServerManager;

    @Getter
    private final AmqpTemplate amqpTemplate;

    @Getter
    private final CipherAlgorithmService cipherAlgorithmService;

    @Getter
    private final RedissonClient redissonClient;

    public AbstractMessageOperation(ChatConfig chatConfig,
                                    MinioTemplate minioTemplate,
                                    SocketServerManager socketServerManager,
                                    AmqpTemplate amqpTemplate,
                                    CipherAlgorithmService cipherAlgorithmService,
                                    RedissonClient redissonClient) {
        this.chatConfig = chatConfig;
        this.minioTemplate = minioTemplate;
        this.socketServerManager = socketServerManager;
        this.amqpTemplate = amqpTemplate;
        this.cipherAlgorithmService = cipherAlgorithmService;
        this.redissonClient = redissonClient;
    }

    /**
     * 获取消息历史文件创建时间
     *
     * @param filename 文件名称
     *
     * @return 创建时间戳
     */
    protected String getHistoryFileCreationTime(String filename) {
        return StringUtils.substringBefore(
                StringUtils.substringAfterLast(filename, "_"),
                ".json"
        );
    }

    /**
     * 判断历史消息文件是否小于指定时间
     *
     * @param filename 文件名称
     * @param time 时间
     *
     * @return true 是，否则 false
     */
    protected boolean isHistoryMessageFileBeforeCurrentTime(String filename, LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);
        LocalDateTime creationTime = LocalDateTime.parse(text, formatter);
        return creationTime.isBefore(time);
    }

    /**
     * 创建历史消息文件名称
     *
     * @param filename 后缀文件名
     *
     * @return 新的历史消息文件名称
     */
    public String createHistoryMessageFilename(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String target = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        String suffix = target + WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + LocalDateTime.now().format(formatter);
        return MessageFormat.format(chatConfig.getMessage().getFileToken(), suffix);
    }

    /**
     * 创建消息
     *
     * @param senderId 发送者 id
     * @param content 发送内容
     * @param messageType 消息类型
     *
     * @return 消息
     */
    public BasicMessageMeta.Message createMessage(Integer senderId, String content, MessageTypeEnum messageType) {
        GlobalMessageMeta.Message message = new GlobalMessageMeta.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(getChatConfig().getCryptoType());
        message.setCryptoKey(getChatConfig().getCryptoKey());
        message.setType(messageType);

        return message;

    }

    /**
     * 获取文件的时间值
     *
     * @param global 全局消息元数据实现
     *
     * @return 时间
     */
    protected LocalDate getFileLocalDate(GlobalMessageMeta global) {

        String filename = global.getCurrentMessageFile();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);

        return  LocalDate.parse(text, formatter);
    }

    /**
     * 设置全局消息元数据当前文件名称
     *
     * @param global 全局消息元数据实现
     * @param globalFilename 当前全局文件名
     * @param historyFileCount 历史文件数量
     *
     * @return 当前全局消息名称
     */
    protected String setGlobalMessageCurrentFilename(GlobalMessageMeta global,
                                                     String globalFilename,
                                                     Integer historyFileCount,
                                                     Bucket bucket) throws Exception {
        String filename = createHistoryMessageFilename(globalFilename);
        global.setCurrentMessageFile(filename);
        global.getMessageFileMap().put(filename, 0);

        if (global.getMessageFileMap().size() > historyFileCount) {
            Optional<String> optional = global
                    .getMessageFileMap()
                    .keySet()
                    .stream()
                    .min(Comparator.comparing(this::getHistoryFileCreationTime));

            if (optional.isPresent()) {
                String minKey = optional.get();
                global.getMessageFileMap().remove(minKey);
            }
        }

        if (MessageTypeEnum.GLOBAL.equals(global.getType())) {
            bucket = getChatConfig().getGlobal().getBucket();
        }

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        getMinioTemplate().writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(getChatConfig().getMessage().getBucket(), global.getCurrentMessageFile());
        getMinioTemplate().writeJsonValue(messageFileObject, new LinkedList<BasicMessageMeta.Message>());

        return global.getCurrentMessageFile();
    }

    /**
     * 保存消息
     *
     * @param messages 消息集合
     * @param globalMessage 全局消息元数据实现
     * @param filename 文件名称
     *
     * @return 带对应文件的消息
     *
     * @throws Exception 保存错误时候抛出
     */
    protected List<BasicMessageMeta.FileMessage> saveMessages(List<GlobalMessageMeta.Message> messages,
                                                              GlobalMessageMeta globalMessage,
                                                              String filename) throws Exception {
        List<BasicMessageMeta.FileMessage> fileMessageList = new LinkedList<>();
        for (GlobalMessageMeta.Message message : messages) {

            String lastMessage = RegExUtils.replaceAll(
                    message.getContent(),
                    SystemConstants.REPLACE_HTML_TAG_REX,
                    StringUtils.EMPTY
            );

            globalMessage.setLastMessage(lastMessage);
            globalMessage.setLastSendTime(new Date());

            BasicMessageMeta.FileMessage fileMessage;
            if (BasicMessageMeta.FileMessage.class.isAssignableFrom(message.getClass())) {
                fileMessage = Casts.cast(message);
            } else {
                fileMessage = BasicMessageMeta.FileMessage.of(message, filename);
            }
            fileMessage.setFilename(filename);

            CipherService cipherService = getCipherAlgorithmService().getCipherService(fileMessage.getCryptoType());

            byte[] key = Base64.decode(fileMessage.getCryptoKey());
            byte[] plainText = fileMessage.getContent().getBytes(StandardCharsets.UTF_8);

            ByteSource cipherText = cipherService.encrypt(plainText, key);
            fileMessage.setContent(cipherText.getBase64());
            fileMessageList.add(fileMessage);
        }

        FileObject messageFileObject = FileObject.of(getChatConfig().getMessage().getBucket(), filename);
        List<GlobalMessageMeta.Message> senderMessageList = getMinioTemplate().readJsonValue(
                messageFileObject,
                new TypeReference<>() {
                }
        );

        if (CollectionUtils.isEmpty(senderMessageList)) {
            senderMessageList = new ArrayList<>();
        }

        senderMessageList.addAll(fileMessageList);
        getMinioTemplate().writeJsonValue(messageFileObject, senderMessageList);

        globalMessage.getMessageFileMap().put(filename, senderMessageList.size());
        FileObject globalFileObject = FileObject.of(globalMessage.getBucketName(), globalMessage.getFilename());
        getMinioTemplate().writeJsonValue(globalFileObject, globalMessage);

        return fileMessageList;
    }

    /**
     * 创建联系人消息
     *
     * @param message 消息实体
     * @param senderId 发送者 id
     * @param recipientId 接收者 id
     * @param type 消息类型
     *
     * @return 联系人消息
     */
    protected ContactMessage<BasicMessageMeta.Message> createContactMessage(GlobalMessageMeta.Message message,
                                                                            Integer senderId,
                                                                            Integer recipientId,
                                                                            MessageTypeEnum type) {

        ContactMessage<BasicMessageMeta.Message> contactMessage = new ContactMessage<>();

        String lastMessage = RegExUtils.replaceAll(
                message.getContent(),
                SystemConstants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        contactMessage.setId(senderId);
        contactMessage.setType(type);
        contactMessage.setTargetId(recipientId);
        contactMessage.setLastSendTime(new Date());
        contactMessage.setLastMessage(lastMessage);
        contactMessage.getMessages().add(message);

        return contactMessage;
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

        byte[] bytes = cipherService.decrypt(Base64.decode(content), Base64.decode(key)).obtainBytes();

        try {
            message.setContent(new String(bytes, CodecUtils.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            String msg = "对内容为 [" + content + "] 的消息通过密钥 [" +
                    key + "] 使用 [" + message.getCryptoType() + "] 解密失败";
            log.warn(msg, e);
        }
    }

    /**
     * 获取全局消息分页
     *
     * @param globalMessage 全局消息元数据实现
     * @param time 时间（在改时间之后的聊天信息）
     * @param pageRequest 分页请求
     *
     * @return 全局消息分页
     */
    protected GlobalMessagePage getGlobalMessagePage(GlobalMessageMeta globalMessage,
                                                     Date time,
                                                     ScrollPageRequest pageRequest,
                                                     Class<? extends GlobalMessageMeta.FileMessage> messageClass) {

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
            List<Map<String, Object>> fileMessageList = getMinioTemplate().readJsonValue(
                    fileObject,
                    new TypeReference<>() {
                    }
            );

            List<GlobalMessageMeta.FileMessage> classList = fileMessageList
                    .stream()
                    .map(m -> Casts.convertValue(m , messageClass))
                    .collect(Collectors.toList());

            List<GlobalMessageMeta.FileMessage> temps = classList
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


    /**
     * 添加未读消息
     *
     * @param targetId       目标 id
     * @param type           消息类型
     * @param contactMessage 联系人消息
     */
    protected void addUnreadMessage(Integer targetId,
                                    MessageTypeEnum type,
                                    ContactMessage<BasicMessageMeta.FileLinkMessage> contactMessage) throws Exception {
        Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> map = getUnreadMessageData(targetId, type);

        ContactMessage<BasicMessageMeta.FileLinkMessage> targetMessage = map.get(contactMessage.getId());

        if (Objects.isNull(targetMessage)) {
            map.put(contactMessage.getId(), contactMessage);
        } else {
            targetMessage.setLastSendTime(contactMessage.getLastSendTime());
            targetMessage.setLastMessage(contactMessage.getLastMessage());
            targetMessage.getMessages().addAll(contactMessage.getMessages());
        }

        FileObject fileObject = getUnreadMessageFileObject(targetId, type);
        getMinioTemplate().writeJsonValue(fileObject, map);
    }

    /**
     * 获取未读消息文件对象
     *
     * @param targetId 目标 id
     * @param type 消息类型
     *
     * @return 未读消息文件对象
     */
    protected FileObject getUnreadMessageFileObject(Integer targetId, MessageTypeEnum type) {
        String filename = MessageFormat.format(
                getChatConfig().getGlobal().getUnreadMessageFileToken(),
                type.toString().toLowerCase(),
                targetId
        );
        return FileObject.of(getChatConfig().getGlobal().getUnreadBucket(), filename);
    }

    /**
     * 创建消息关联文件的实体
     *
     * @param message        消息实体
     * @param sourceMessages 来源消息集合
     *
     * @return 消息关联文件的实体
     */
    protected BasicMessageMeta.FileLinkMessage createFileLinkMessage(BasicMessageMeta.FileMessage message,
                                                                   List<BasicMessageMeta.FileMessage> sourceMessages) {

        BasicMessageMeta.FileLinkMessage result = Casts.of(message, BasicMessageMeta.FileLinkMessage.class);
        result.getFilenames().add(message.getFilename());

        sourceMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .forEach(s -> result.getFilenames().add(s.getFilename()));

        return result;
    }

    /**
     * 获取未读消息数据
     *
     * @param targetId 目标 id
     * @param type 消息类型
     *
     * @return 未读消息数据
     */
    protected Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> getUnreadMessageData(Integer targetId,
                                                                                                  MessageTypeEnum type) {
        String filename = MessageFormat.format(
                getChatConfig().getGlobal().getUnreadMessageFileToken(),
                type.toString().toLowerCase(),
                targetId
        );
        FileObject fileObject = FileObject.of(getChatConfig().getGlobal().getUnreadBucket(), filename);
        Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> map = getMinioTemplate().readJsonValue(
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
        Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> map = getUnreadMessageData(body.getReaderId(), MessageTypeEnum.CONTACT);

        if (MapUtils.isEmpty(map)) {
            return ;
        }

        ContactMessage<BasicMessageMeta.FileLinkMessage> message = map.get(body.getTargetId());

        if (Objects.isNull(message)) {
            return ;
        }

        List<BasicMessageMeta.FileLinkMessage> fileLinkMessages = message
                .getMessages()
                .stream()
                .filter(umb -> body.getMessageIds().contains(umb.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(fileLinkMessages)) {
            return;
        }

        for (BasicMessageMeta.FileLinkMessage fileLinkMessage : fileLinkMessages) {

            List<FileObject> list = fileLinkMessage
                    .getFilenames()
                    .stream()
                    .map(f -> FileObject.of(getChatConfig().getMessage().getBucket(), f))
                    .collect(Collectors.toList());

            for (FileObject messageFileObject : list) {
                doReadMessage(messageFileObject, fileLinkMessage, body);
            }
        }
        postReadMessage(map, fileLinkMessages, body);

    }

    protected abstract void postReadMessage(Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> unreadMessageData,
                                            List<BasicMessageMeta.FileLinkMessage> fileLinkMessages,
                                            ReadMessageRequestBody body) throws Exception;

    protected abstract void doReadMessage(FileObject messageFileObject,
                                          BasicMessageMeta.FileLinkMessage fileLinkMessage,
                                          ReadMessageRequestBody body) throws Exception;

    @Override
    public void afterPropertiesSet() throws Exception {
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getGlobal().getBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getGlobal().getUnreadBucket());
        getMinioTemplate().makeBucketIfNotExists(getChatConfig().getMessage().getBucket());
        install();
    }

    protected void install() throws Exception {

    }
}
