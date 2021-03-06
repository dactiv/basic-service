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
 * ???????????????????????????????????????
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
            exception = "??????????????????????????????"
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
        // ??????????????????????????????
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
        // ??????????????????????????????????????????????????????????????????????????????????????????
        //noinspection unchecked
        ContactMessage<BasicMessageMeta.FileLinkMessage> recipientMessage = Casts.of(
                contactMessage,
                ContactMessage.class
        );

        List<BasicMessageMeta.ContactReadableMessage> sourceMessages = new ArrayList<>(List.copyOf(sourceUserMessages));
        sourceMessages.addAll(List.copyOf(globalMessages));
        // ?????? sourceUserMessages ??? globalMessages ???????????????????????????????????????
        List<BasicMessageMeta.FileLinkMessage> fileLinkMessages = targetUserMessages
                .stream()
                .map(m -> this.createFileLinkMessage(m, sourceMessages))
                .peek(m -> m.setContent(message.getContent()))
                .collect(Collectors.toList());
        // ?????? ContactMessage ?????? messages ????????? new ????????????copy bean ?????????????????????????????????
        // ???????????????????????? contactMessage.getMessages().add(message); ?????????????????? list ????????? message ?????????
        // ????????????????????????????????????????????? recipientMessage ????????????????????????
        recipientMessage.setMessages(fileLinkMessages);
        // ??????????????????
        addUnreadMessage(recipientId, MessageTypeEnum.CONTACT, recipientMessage);

        //noinspection unchecked
        ContactMessage<BasicMessageMeta.ContactReadableMessage> unicastMessage = Casts.of(contactMessage, ContactMessage.class);
        unicastMessage.setMessages(targetUserMessages);
        unicastMessage.getMessages().forEach(this::decryptMessageContent);

        SocketUserDetails userDetails = getSocketServerManager().getSocketUserDetails(recipientId);
        // ???????????????????????????????????????????????????
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
     * ????????????????????? id ??????
     *
     * @param userId ?????? id
     *
     * @return ??????????????? id ??????
     */
    public List<RecentContactMeta> getRecentContacts(Integer userId) {
        List<RecentContactMeta> idEntities = getRecentContactData(userId);

        return idEntities
                .stream()
                .sorted(Comparator.comparing(IntegerIdEntity::getCreationTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * ???????????????????????????
     *
     * @param userId ?????? id
     *
     * @return ???????????????????????????
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
     * ?????????????????????????????????
     *
     * @param userId ?????? id
     *
     * @return ???????????????????????????
     */
    public FileObject getRecentContactFileObject(Integer userId) {
        String filename = MessageFormat.format(getChatConfig().getContact().getRecentFileToken(), userId);
        return FileObject.of(getChatConfig().getContact().getRecentBucket(), filename);
    }

    /**
     * ?????????????????????
     *
     * @param userId    ?????? id
     * @param contactId ????????? id
     * @param type      ???????????????
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
     * ????????????????????????
     *
     * @param messages ????????????
     * @param sourceId ?????? id(??????????????? id)
     * @param targetId ?????? id(??????????????? id)
     * @param global   ????????????????????????????????????????????????????????????
     *
     * @throws Exception ?????????????????????????????????
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
     * ??????????????????
     *
     * @param sourceId ?????? id (??????????????? id)
     * @param targetId ?????? id (??????????????? id)
     * @param global   ????????????????????????????????????????????????????????????
     *
     * @return ????????????
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
     * ?????? redis ???????????????
     *
     * @param filename ????????????
     * @param global   ??????????????????????????????
     *
     * @return redis ???
     */
    private RBucket<GlobalMessageMeta> getRedisGlobalMessageBucket(String filename, boolean global) {
        String key = StringUtils.substringBeforeLast(filename, Casts.DEFAULT_DOT_SYMBOL);

        CacheProperties cache = getGlobalMessageCacheProperties(global);

        return getRedissonClient().getBucket(cache.getName(key));
    }

    /**
     * ??????????????????????????????
     *
     * @param global ?????????????????????true ???????????? ????????????
     *
     * @return ????????????
     */
    private CacheProperties getGlobalMessageCacheProperties(boolean global) {
        return global ? getChatConfig().getGlobal().getCache() : getChatConfig().getContact().getCache();
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param global   ????????????
     * @param sourceId ?????? id(??????????????? id)
     * @param targetId ?????? id(??????????????? id)
     *
     * @return ????????????
     *
     * @throws Exception ?????????????????????
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
     * ????????????????????????
     *
     * @param global   ????????????
     * @param sourceId ?????? id(??????????????? id)
     * @param targetId ?????? id(??????????????? id)
     *
     * @return ????????????
     *
     * @throws Exception ???????????????????????????
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
