package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.model.BasicMessageModel;
import com.github.dactiv.basic.socket.server.domain.model.GlobalMessageModel;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.model.RecentContactModel;
import com.github.dactiv.basic.socket.server.enumerate.ContactTypeEnum;
import com.github.dactiv.basic.socket.server.enumerate.GlobalMessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.SaveMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;

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

import static com.github.dactiv.basic.commons.Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 聊天业务逻辑服务
 *
 * @author maurice.chen
 */
@Slf4j
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

    @Getter
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
     * 获取消息分页
     *
     * @param userId      用户 id
     * @param targetId    目标用户 id
     * @param pageRequest 分页请求
     *
     * @return 全局消息分页
     */
    public GlobalMessagePage getHistoryMessagePage(Integer userId,
                                                   Integer targetId,
                                                   Date time,
                                                   ScrollPageRequest pageRequest) {

        GlobalMessageModel globalMessage = getGlobalMessage(userId, targetId, false);

        List<GlobalMessageModel.FileMessage> messages = new LinkedList<>();
        LocalDateTime dateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
        List<String> historyFiles = globalMessage
                .getMessageFileMap()
                .keySet()
                .stream()
                .filter(s -> this.isHistoryMessageFileBeforeCurrentTime(s, dateTime))
                .sorted(Comparator.comparing(this::getHistoryFileCreationTime).reversed())
                .collect(Collectors.toList());

        for (String file : historyFiles) {
            FileObject fileObject = FileObject.of(chatConfig.getMessage().getBucket(), file);
            List<GlobalMessageModel.FileMessage> fileMessageList = minioTemplate.readJsonValue(
                    fileObject,
                    new TypeReference<>() {
                    }
            );

            List<GlobalMessageModel.FileMessage> temps = fileMessageList
                    .stream()
                    .filter(f -> f.getCreationTime().before(time))
                    .sorted(Comparator.comparing(BasicMessageModel.Message::getCreationTime).reversed())
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
     * 获取历史消息日期集合
     *
     * @param userId 用户 id
     * @param targetId 目标用户 id
     *
     * @return 历史消息日期集合
     */
    public List<Date> getHistoryMessageDateList(Integer userId, Integer targetId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        GlobalMessageModel globalMessage = getGlobalMessage(userId, targetId, false);
        return  globalMessage
                .getMessageFileMap()
                .keySet()
                .stream()
                .map(this::getHistoryFileCreationTime)
                .map(k -> LocalDateTime.parse(k, formatter))
                .map(ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
        String text = getHistoryFileCreationTime(filename);
        LocalDateTime creationTime = LocalDateTime.parse(text, formatter);
        return creationTime.isBefore(time);
    }

    /**
     * 解密消息内容
     *
     * @param message 文件消息
     */
    private void decryptMessageContent(BasicMessageModel.FileMessage message) {
        CipherService cipherService = cipherAlgorithmService.getCipherService(message.getCryptoType());
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
     * 获取 redis 全局消息桶
     *
     * @param filename 文件名称
     *
     * @return redis 桶
     */
    private RBucket<GlobalMessageModel> getRedisGlobalMessageBucket(String filename, boolean global) {
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
     * @param sourceId 来源 id (发送者用户 id)
     * @param targetId 目标 id (收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @return 全局消息
     */
    public GlobalMessageModel getGlobalMessage(Integer sourceId, Integer targetId, boolean global) {

        String filename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);

        Bucket minioBucket = chatConfig.getContact().getContactBucket();

        if (global) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);

            filename = MessageFormat.format(chatConfig.getGlobal().getPersonFileToken(), min, max);
            minioBucket = chatConfig.getGlobal().getBucket();
        }

        RBucket<GlobalMessageModel> redisBucket = getRedisGlobalMessageBucket(filename, global);

        GlobalMessageModel globalMessage = redisBucket.get();

        if (Objects.isNull(globalMessage)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            globalMessage = minioTemplate.readJsonValue(fileObject, GlobalMessageModel.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessageModel();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(minioBucket.getBucketName());
            globalMessage.setType(global ? GlobalMessageTypeEnum.Global.getValue() : GlobalMessageTypeEnum.Contact.getValue());
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
    private String getGlobalMessageCurrentFilename(GlobalMessageModel global, Integer sourceId, Integer targetId) throws Exception {
        String filename;

        if (MapUtils.isNotEmpty(global.getMessageFileMap())) {
            filename = global.getCurrentMessageFile();
            Integer count = global.getMessageFileMap().get(filename);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(chatConfig.getMessage().getFileSuffix());
            String text = getHistoryFileCreationTime(filename);

            LocalDate before = LocalDate.parse(text, formatter);
            LocalDate now = LocalDate.now();

            if (count > chatConfig.getMessage().getBatchSize() || ChronoUnit.DAYS.between(before, now) >= 1) {
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
    private String createHistoryMessageFile(GlobalMessageModel global, Integer sourceId, Integer targetId) throws Exception {
        String globalFilename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);
        Integer historyFileCount = chatConfig.getContact().getHistoryMessageFileCount();

        if (GlobalMessageTypeEnum.Global.getValue().equals(global.getType())) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);
            globalFilename = MessageFormat.format(chatConfig.getGlobal().getPersonFileToken(), min, max);
            historyFileCount = chatConfig.getGlobal().getHistoryMessageFileCount();
        }

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

        Bucket bucket = chatConfig.getContact().getContactBucket();

        if (GlobalMessageTypeEnum.Global.getValue().equals(global.getType())) {
            bucket = chatConfig.getGlobal().getBucket();
        }

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        minioTemplate.writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        minioTemplate.writeJsonValue(messageFileObject, new LinkedList<BasicMessageModel.Message>());

        return filename;
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
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessageModel.FileMessage> addHistoryMessage(List<GlobalMessageModel.Message> messages,
                                                                 Integer sourceId,
                                                                 Integer targetId,
                                                                 boolean global) throws Exception {

        GlobalMessageModel globalMessage = getGlobalMessage(sourceId, targetId, global);
        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);
        List<BasicMessageModel.FileMessage> fileMessageList = new LinkedList<>();
        for (GlobalMessageModel.Message message : messages) {

            String lastMessage = RegExUtils.replaceAll(
                    message.getContent(),
                    Constants.REPLACE_HTML_TAG_REX,
                    StringUtils.EMPTY
            );

            globalMessage.setLastMessage(lastMessage);
            globalMessage.setLastSendTime(new Date());

            BasicMessageModel.FileMessage fileMessage = BasicMessageModel.FileMessage.of(message, filename);
            CipherService cipherService = cipherAlgorithmService.getCipherService(fileMessage.getCryptoType());

            byte[] key = Base64.decode(fileMessage.getCryptoKey());
            byte[] plainText = fileMessage.getContent().getBytes(StandardCharsets.UTF_8);

            ByteSource cipherText = cipherService.encrypt(plainText, key);
            fileMessage.setContent(cipherText.getBase64());
            fileMessageList.add(fileMessage);
        }

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        List<GlobalMessageModel.Message> senderMessageList = minioTemplate.readJsonValue(
                messageFileObject,
                new TypeReference<>() {
                }
        );

        if (CollectionUtils.isEmpty(senderMessageList)) {
            senderMessageList = new ArrayList<>();
        }

        senderMessageList.addAll(fileMessageList);
        minioTemplate.writeJsonValue(messageFileObject, senderMessageList);

        globalMessage.getMessageFileMap().put(filename, senderMessageList.size());
        FileObject globalFileObject = FileObject.of(globalMessage.getBucketName(), globalMessage.getFilename());
        minioTemplate.writeJsonValue(globalFileObject, globalMessage);

        RBucket<GlobalMessageModel> bucket = getRedisGlobalMessageBucket(globalMessage.getFilename(), global);
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
    public GlobalMessageModel.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {

        GlobalMessageModel.Message message = new GlobalMessageModel.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(chatConfig.getCryptoType());
        message.setCryptoKey(chatConfig.getCryptoKey());

        List<BasicMessageModel.FileMessage> sourceUserMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                false
        );
        List<BasicMessageModel.FileMessage> targetUserMessages = addHistoryMessage(
                Collections.singletonList(message),
                recipientId,
                senderId,
                false
        );
        // 添加全局聊天记录文件
        List<BasicMessageModel.FileMessage> globalMessages = addHistoryMessage(
                Collections.singletonList(message),
                senderId,
                recipientId,
                true
        );

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(recipientId);

        ContactMessage<BasicMessageModel.Message> contactMessage = new ContactMessage<>();

        String lastMessage = RegExUtils.replaceAll(
                message.getContent(),
                Constants.REPLACE_HTML_TAG_REX,
                StringUtils.EMPTY
        );

        contactMessage.setId(senderId);
        contactMessage.setType(ContactTypeEnum.Person.getValue());
        contactMessage.setTargetId(recipientId);
        contactMessage.setLastSendTime(new Date());
        contactMessage.setLastMessage(lastMessage);
        contactMessage.getMessages().add(message);

        List<BasicMessageModel.UserMessageBody> userMessageBodies = targetUserMessages
                .stream()
                .map(m -> this.createUserMessageBody(m, sourceUserMessages, globalMessages))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());

        // 构造未读消息内容，用于已读时能够更改所有文件的状态为已读
        //noinspection unchecked
        ContactMessage<BasicMessageModel.UserMessageBody> recipientMessage = Casts.of(contactMessage, ContactMessage.class);
        // 由于 ContactMessage 类的 messages 字段是 new 出来的，copy bean 会注解将对象引用到字段中，
        // 而下面由调用了 contactMessage.getMessages().add(message); 就会产生这个 list 由两条 message记录，
        // 所以在这里直接对一个新的集合给 recipientMessage 隔离开来添加数据
        recipientMessage.setMessages(userMessageBodies);
        // 保存未读记录
        addUnreadMessage(recipientId, recipientMessage);

        //noinspection unchecked
        ContactMessage<BasicMessageModel.FileMessage> unicastMessage = Casts.of(contactMessage, ContactMessage.class);
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
            socketServerManager.saveTempMessage(
                    recipientId,
                    CHAT_MESSAGE_EVENT_NAME,
                    unicastMessage
            );
        }

        amqpTemplate.convertAndSend(
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
    private BasicMessageModel.UserMessageBody createUserMessageBody(BasicMessageModel.FileMessage message,
                                                                    List<BasicMessageModel.FileMessage> sourceMessages,
                                                                    List<BasicMessageModel.FileMessage> globalMessages) {

        BasicMessageModel.UserMessageBody result = Casts.of(message, BasicMessageModel.UserMessageBody.class);
        result.getFilenames().add(message.getFilename());

        BasicMessageModel.FileMessage sourceUserMessage = sourceMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + message.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(sourceUserMessage.getFilename());

        BasicMessageModel.FileMessage globalMessage = globalMessages
                .stream()
                .filter(r -> r.getId().equals(result.getId()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 ID 为 [" + message.getId() + "] 的对应来源消息数据"));

        result.getFilenames().add(globalMessage.getFilename());

        return result;
    }

    /**
     * 获取未读消息集合
     *
     * @param userId 用户 id
     *
     * @return 未读消息集合
     */
    public List<ContactMessage<BasicMessageModel.UserMessageBody>> getUnreadMessages(Integer userId) {
        Map<Integer, ContactMessage<BasicMessageModel.UserMessageBody>> result = getUnreadMessageData(userId);

        return result
                .values()
                .stream()
                .sorted(Comparator.comparing(BasicMessageModel::getLastMessage).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 添加未读消息
     *
     * @param userId         用户 id
     * @param contactMessage 联系人消息
     */
    @Deprecated
    private void addUnreadMessage(Integer userId, ContactMessage<BasicMessageModel.UserMessageBody> contactMessage) throws Exception {
        Map<Integer, ContactMessage<BasicMessageModel.UserMessageBody>> map = getUnreadMessageData(userId);

        ContactMessage<BasicMessageModel.UserMessageBody> targetMessage = map.get(contactMessage.getId());

        if (Objects.isNull(targetMessage)) {
            map.put(contactMessage.getId(), contactMessage);
        } else {
            targetMessage.setLastSendTime(contactMessage.getLastSendTime());
            targetMessage.setLastMessage(contactMessage.getLastMessage());
            targetMessage.getMessages().addAll(contactMessage.getMessages());
        }

        FileObject fileObject = getUnreadMessageFileObject(userId);
        minioTemplate.writeJsonValue(fileObject, map);
    }

    /**
     * 获取未读消息数据
     *
     * @param userId 用户 id
     *
     * @return 未读消息数据
     */
    @Deprecated
    public Map<Integer, ContactMessage<BasicMessageModel.UserMessageBody>> getUnreadMessageData(Integer userId) {
        String filename = MessageFormat.format(chatConfig.getContact().getUnreadMessageFileToken(), userId);
        FileObject fileObject = FileObject.of(chatConfig.getContact().getUnreadBucket(), filename);
        Map<Integer, ContactMessage<BasicMessageModel.UserMessageBody>> map = minioTemplate.readJsonValue(
                fileObject,
                new TypeReference<>() {
                }
        );

        if (MapUtils.isEmpty(map)) {
            map = new LinkedHashMap<>();
        }

        return map;
    }

    /**
     * 获取未读消息文件对象
     *
     * @param userId 用户 id
     *
     * @return 未读消息文件对象
     */
    @Deprecated
    public FileObject getUnreadMessageFileObject(Integer userId) {
        String filename = MessageFormat.format(chatConfig.getContact().getUnreadMessageFileToken(), userId);
        return FileObject.of(chatConfig.getContact().getUnreadBucket(), filename);
    }

    /**
     * 获取常用联系人 id 集合
     *
     * @param userId 用户 id
     *
     * @return 常用联系人 id 集合
     */
    public List<RecentContactModel> getRecentContacts(Integer userId) {
        List<RecentContactModel> idEntities = getRecentContactData(userId);

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
    public List<RecentContactModel> getRecentContactData(Integer userId) {
        String filename = MessageFormat.format(chatConfig.getContact().getRecentFileToken(), userId);
        FileObject fileObject = FileObject.of(chatConfig.getContact().getRecentBucket(), filename);
        List<RecentContactModel> recentContacts = minioTemplate.readJsonValue(fileObject, new TypeReference<>() {});

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
        String filename = MessageFormat.format(chatConfig.getContact().getRecentFileToken(), userId);
        return FileObject.of(chatConfig.getContact().getRecentBucket(), filename);
    }

    /**
     * 添加常用联系人
     *
     * @param userId    用户 id
     * @param contactId 联系人 id
     * @param type 联系人类型
     */
    public void addRecentContact(Integer userId, Integer contactId, ContactTypeEnum type) throws Exception {
        List<RecentContactModel> recentContacts = getRecentContactData(userId);

        RecentContactModel recentContact = recentContacts
                .stream()
                .filter(i -> i.getId().equals(contactId) && i.getType().equals(type.getValue()))
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(recentContact)) {
            recentContact.setCreationTime(new Date());
        } else {
            recentContact = new RecentContactModel();
            recentContact.setId(contactId);
            recentContact.setType(type.getValue());
            recentContacts.add(recentContact);
        }

        for (int i = 0; i < recentContacts.size() - chatConfig.getContact().getRecentCount(); i++) {

            Optional<RecentContactModel> optional = recentContacts
                    .stream()
                    .min(Comparator.comparing(IntegerIdEntity::getCreationTime));

            if (optional.isEmpty()) {
                break;
            }

            recentContacts.removeIf(entity -> entity.getId().equals(optional.get().getId()));
        }
        FileObject fileObject = getRecentContactFileObject(userId);
        minioTemplate.writeJsonValue(fileObject, recentContacts);
    }

    /**
     * 读取信息
     *
     * @param body 读取消息 request body
     */
    @SocketMessage
    public void readMessage(ReadMessageRequestBody body) throws Exception {

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(body.getSenderId());

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, body.getRecipientId(),
                            IdentityNamingStrategy.TYPE_KEY, ContactTypeEnum.Person.getValue(),
                            GlobalMessageModel.DEFAULT_MESSAGE_IDS, body.getMessageIds()
                    )
            );
        } else {
            socketServerManager.saveTempMessage(
                    body.getSenderId(),
                    CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(
                            IdEntity.ID_FIELD_NAME, body.getRecipientId(),
                            GlobalMessageModel.DEFAULT_MESSAGE_IDS, body.getMessageIds()
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
