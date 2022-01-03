package com.github.dactiv.basic.socket.server.service.chat.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.chat.AbstractMessageOperation;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    public GlobalMessagePage getHistoryMessagePage(Integer userId, Integer targetId, Date time, ScrollPageRequest pageRequest) {
        return null;
    }

    @Override
    public List<Date> getHistoryMessageDateList(Integer userId, Integer targetId) {
        return null;
    }

    @Override
    public void readMessage(ReadMessageRequestBody body) throws Exception {

    }

    @Override
    public void consumeReadMessage(ReadMessageRequestBody body) throws Exception {

    }

    @Override
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @Concurrent(
            value = "socket:chat:group:send:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception {
        GlobalMessageMeta.Message message = createMessage(senderId, content, MessageTypeEnum.GROUP);
        message.setType(MessageTypeEnum.GROUP);

        return null;
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
        CacheProperties cache = getChatConfig().getGroup().getCache();

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

        setGlobalMessageCurrentFilename(global, globalFilename, historyFileCount);

        Bucket bucket = getChatConfig().getGlobal().getBucket();

        FileObject globalFileObject = FileObject.of(bucket, globalFilename);
        getMinioTemplate().writeJsonValue(globalFileObject, global);

        FileObject messageFileObject = FileObject.of(getChatConfig().getMessage().getBucket(), global.getCurrentMessageFile());
        getMinioTemplate().writeJsonValue(messageFileObject, new LinkedList<BasicMessageMeta.Message>());

        return global.getCurrentMessageFile();
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

        Bucket minioBucket = getChatConfig().getContact().getContactBucket();

        RBucket<GlobalMessageMeta> redisBucket = getRedisGlobalMessageBucket(filename);

        GlobalMessageMeta globalMessage = redisBucket.get();

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

        CacheProperties cache = getChatConfig().getGroup().getCache();

        return getRedissonClient().getBucket(cache.getName(key));
    }
}
