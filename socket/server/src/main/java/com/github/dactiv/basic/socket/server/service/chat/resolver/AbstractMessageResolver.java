package com.github.dactiv.basic.socket.server.service.chat.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.GlobalMessageTypeEnum;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
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
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.WebDataBinder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 抽象的消息解析实现，用于分离一下可复用的公共方法
 *
 * @author maurice.chen
 */
@Slf4j
public abstract class AbstractMessageResolver implements MessageResolver{

    @Getter
    private final ChatConfig chatConfig;

    @Getter
    private final MinioTemplate minioTemplate;

    @Getter
    private final SocketServerManager socketServerManager;

    @Getter
    private final AmqpTemplate amqpTemplate;

    private final CipherAlgorithmService cipherAlgorithmService;

    private final RedissonClient redissonClient;

    public AbstractMessageResolver(ChatConfig chatConfig,
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
     * 获取全局消息
     *
     * @param sourceId 来源 id (发送者用户 id)
     * @param targetId 目标 id (收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @return 全局消息
     */
    public GlobalMessageMeta getGlobalMessage(Integer sourceId, Integer targetId, boolean global) {

        String filename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);

        Bucket minioBucket = chatConfig.getContact().getContactBucket();

        if (global) {
            Integer min = Math.min(sourceId, targetId);
            Integer max = Math.max(sourceId, targetId);

            filename = MessageFormat.format(chatConfig.getGlobal().getPersonFileToken(), min, max);
            minioBucket = chatConfig.getGlobal().getBucket();
        }

        RBucket<GlobalMessageMeta> redisBucket = getRedisGlobalMessageBucket(filename, global);

        GlobalMessageMeta globalMessage = redisBucket.get();

        if (Objects.isNull(globalMessage)) {
            FileObject fileObject = FileObject.of(minioBucket, filename);
            globalMessage = minioTemplate.readJsonValue(fileObject, GlobalMessageMeta.class);
        }

        if (Objects.isNull(globalMessage)) {
            globalMessage = new GlobalMessageMeta();

            globalMessage.setFilename(filename);
            globalMessage.setBucketName(minioBucket.getBucketName());
            globalMessage.setType(global ? GlobalMessageTypeEnum.GLOBAL : GlobalMessageTypeEnum.CONTACT);
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
    private RBucket<GlobalMessageMeta> getRedisGlobalMessageBucket(String filename, boolean global) {
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
     * 解密消息内容
     *
     * @param message 文件消息
     */
    protected void decryptMessageContent(BasicMessageMeta.FileMessage message) {
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
     * 添加历史记录消息
     *
     * @param messages 消息内容
     * @param sourceId 来源 id(发送者用户 id)
     * @param targetId 目标 id(收信者用户 id)
     * @param global   是否全局消息（由系统保存的历史记录消息）
     *
     * @throws Exception 存储消息记录失败时抛出
     */
    public List<BasicMessageMeta.FileMessage> addHistoryMessage(List<GlobalMessageMeta.Message> messages,
                                                                Integer sourceId,
                                                                Integer targetId,
                                                                boolean global) throws Exception {

        GlobalMessageMeta globalMessage = getGlobalMessage(sourceId, targetId, global);
        String filename = getGlobalMessageCurrentFilename(globalMessage, sourceId, targetId);
        List<BasicMessageMeta.FileMessage> fileMessageList = new LinkedList<>();
        for (GlobalMessageMeta.Message message : messages) {

            String lastMessage = RegExUtils.replaceAll(
                    message.getContent(),
                    SystemConstants.REPLACE_HTML_TAG_REX,
                    StringUtils.EMPTY
            );

            globalMessage.setLastMessage(lastMessage);
            globalMessage.setLastSendTime(new Date());

            BasicMessageMeta.FileMessage fileMessage = BasicMessageMeta.FileMessage.of(message, filename);
            CipherService cipherService = cipherAlgorithmService.getCipherService(fileMessage.getCryptoType());

            byte[] key = Base64.decode(fileMessage.getCryptoKey());
            byte[] plainText = fileMessage.getContent().getBytes(StandardCharsets.UTF_8);

            ByteSource cipherText = cipherService.encrypt(plainText, key);
            fileMessage.setContent(cipherText.getBase64());
            fileMessageList.add(fileMessage);
        }

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        List<GlobalMessageMeta.Message> senderMessageList = minioTemplate.readJsonValue(
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

        RBucket<GlobalMessageMeta> bucket = getRedisGlobalMessageBucket(globalMessage.getFilename(), global);
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
        String globalFilename = MessageFormat.format(chatConfig.getContact().getFileToken(), sourceId, targetId);
        Integer historyFileCount = chatConfig.getContact().getHistoryMessageFileCount();

        if (GlobalMessageTypeEnum.GLOBAL.equals(global.getType())) {
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

        if (GlobalMessageTypeEnum.GLOBAL.equals(global.getType())) {
            bucket = chatConfig.getGlobal().getBucket();
        }

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        minioTemplate.writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(chatConfig.getMessage().getBucket(), filename);
        minioTemplate.writeJsonValue(messageFileObject, new LinkedList<BasicMessageMeta.Message>());

        return filename;
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
}
