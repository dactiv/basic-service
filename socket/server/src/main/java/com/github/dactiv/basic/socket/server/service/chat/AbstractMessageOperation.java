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
import com.github.dactiv.framework.commons.ReflectionUtils;
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
 * ????????????????????????????????????????????????????????????????????????
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class AbstractMessageOperation<T extends BasicMessageMeta.FileMessage> implements MessageOperation, InitializingBean {

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

    protected final Class<T> messageType;

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

        this.messageType = ReflectionUtils.getGenericClass(getClass(), 0);
    }

    /**
     * ????????????????????????????????????
     *
     * @param filename ????????????
     *
     * @return ???????????????
     */
    protected String getHistoryFileCreationTime(String filename) {
        return StringUtils.substringBefore(
                StringUtils.substringAfterLast(filename, "_"),
                ".json"
        );
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param filename ????????????
     * @param time     ??????
     *
     * @return true ???????????? false
     */
    protected boolean isHistoryMessageFileBeforeCurrentTime(String filename, LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);
        LocalDateTime creationTime = LocalDateTime.parse(text, formatter);
        return creationTime.isBefore(time);
    }

    /**
     * ??????????????????????????????
     *
     * @param filename ???????????????
     *
     * @return ??????????????????????????????
     */
    public String createHistoryMessageFilename(String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String target = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);
        String suffix = target + WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + LocalDateTime.now().format(formatter);
        return MessageFormat.format(chatConfig.getMessage().getFileToken(), suffix);
    }

    /**
     * ????????????
     *
     * @param senderId    ????????? id
     * @param content     ????????????
     * @param messageType ????????????
     *
     * @return ??????
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
     * ????????????????????????
     *
     * @param global ???????????????????????????
     *
     * @return ??????
     */
    protected LocalDate getFileLocalDate(GlobalMessageMeta global) {

        String filename = global.getCurrentMessageFile();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getChatConfig().getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);

        return LocalDate.parse(text, formatter);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param global           ???????????????????????????
     * @param globalFilename   ?????????????????????
     * @param historyFileCount ??????????????????
     *
     * @return ????????????????????????
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
     * ????????????
     *
     * @param messages      ????????????
     * @param globalMessage ???????????????????????????
     * @param filename      ????????????
     *
     * @return ????????????????????????
     *
     * @throws Exception ????????????????????????
     */
    protected List<T> saveMessages(List<T> messages, GlobalMessageMeta globalMessage, String filename) throws Exception {
        List<T> fileMessageList = new LinkedList<>();
        for (T message : messages) {

            String lastMessage = RegExUtils.replaceAll(
                    message.getContent(),
                    SystemConstants.REPLACE_HTML_TAG_REX,
                    StringUtils.EMPTY
            );

            globalMessage.setLastMessage(lastMessage);
            globalMessage.setLastSendTime(new Date());

            message.setFilename(filename);

            CipherService cipherService = getCipherAlgorithmService().getCipherService(message.getCryptoType());

            byte[] key = Base64.decode(message.getCryptoKey());
            byte[] plainText = message.getContent().getBytes(StandardCharsets.UTF_8);

            ByteSource cipherText = cipherService.encrypt(plainText, key);
            message.setContent(cipherText.getBase64());
            fileMessageList.add(message);
        }

        FileObject messageFileObject = FileObject.of(getChatConfig().getMessage().getBucket(), filename);
        List<T> senderMessageList = getMinioTemplate().readJsonValue(
                messageFileObject,
                Casts.getObjectMapper().getTypeFactory().constructCollectionType(List.class, messageType)
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
     * ?????????????????????
     *
     * @param message     ????????????
     * @param senderId    ????????? id
     * @param recipientId ????????? id
     * @param type        ????????????
     *
     * @return ???????????????
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
     * ??????????????????
     *
     * @param message ????????????
     */
    protected void decryptMessageContent(T message) {
        CipherService cipherService = getCipherAlgorithmService().getCipherService(message.getCryptoType());
        String content = message.getContent();
        String key = message.getCryptoKey();

        byte[] bytes = cipherService.decrypt(Base64.decode(content), Base64.decode(key)).obtainBytes();

        try {
            message.setContent(new String(bytes, CodecUtils.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            String msg = "???????????? [" + content + "] ????????????????????? [" +
                    key + "] ?????? [" + message.getCryptoType() + "] ????????????";
            log.warn(msg, e);
        }
    }

    /**
     * ????????????????????????
     *
     * @param globalMessage ???????????????????????????
     * @param time          ?????????????????????????????????????????????
     * @param pageRequest   ????????????
     *
     * @return ??????????????????
     */
    protected GlobalMessagePage getGlobalMessagePage(GlobalMessageMeta globalMessage,
                                                     Date time,
                                                     ScrollPageRequest pageRequest) {

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
            List<T> fileMessageList = getMinioTemplate().readJsonValue(
                    fileObject,
                    Casts.getObjectMapper().getTypeFactory().constructCollectionType(List.class, messageType)
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

    /**
     * ??????????????????
     *
     * @param targetId       ?????? id
     * @param type           ????????????
     * @param contactMessage ???????????????
     */
    protected void addUnreadMessage(Integer targetId,
                                    MessageTypeEnum type,
                                    ContactMessage<BasicMessageMeta.FileLinkMessage> contactMessage) throws Exception {
        Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> map = getUnreadMessageData(targetId, type);

        Integer id = getUnreadMessageMapDataTargetId(contactMessage);

        ContactMessage<BasicMessageMeta.FileLinkMessage> targetMessage = map.get(id);

        if (Objects.isNull(targetMessage)) {
            map.put(id, contactMessage);
        } else {
            targetMessage.setLastSendTime(contactMessage.getLastSendTime());
            targetMessage.setLastMessage(contactMessage.getLastMessage());
            targetMessage.getMessages().addAll(contactMessage.getMessages());
        }

        FileObject fileObject = getUnreadMessageFileObject(targetId, type);
        getMinioTemplate().writeJsonValue(fileObject, map);
    }

    /**
     * ??????????????????????????????
     *
     * @param targetId ?????? id
     * @param type     ????????????
     *
     * @return ????????????????????????
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
     * ?????????????????????????????????
     *
     * @param message        ????????????
     * @param sourceMessages ??????????????????
     *
     * @return ???????????????????????????
     */
    protected BasicMessageMeta.FileLinkMessage createFileLinkMessage(T message, List<T> sourceMessages) {

        BasicMessageMeta.FileLinkMessage result = Casts.of(message, BasicMessageMeta.FileLinkMessage.class);
        result.getFilenames().add(message.getFilename());

        sourceMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .forEach(s -> result.getFilenames().add(s.getFilename()));

        return result;
    }

    /**
     * ????????????????????????
     *
     * @param targetId ?????? id
     * @param type     ????????????
     *
     * @return ??????????????????
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
        Integer id = getUnreadMessageDataTargetId(body);
        Map<Integer, ContactMessage<BasicMessageMeta.FileLinkMessage>> map = getUnreadMessageData(id, body.getType());
        if (MapUtils.isEmpty(map)) {
            return;
        }

        ContactMessage<BasicMessageMeta.FileLinkMessage> message = map.get(body.getTargetId());

        if (Objects.isNull(message)) {
            return;
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

    /**
     * ??????????????????????????????????????? id
     *
     * @param body ?????????????????????
     *
     * @return ?????? id
     */
    protected Integer getUnreadMessageDataTargetId(ReadMessageRequestBody body) {
        return body.getReaderId();
    }

    protected Integer getUnreadMessageMapDataTargetId(ContactMessage<BasicMessageMeta.FileLinkMessage> contactMessage) {
        return contactMessage.getId();
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
