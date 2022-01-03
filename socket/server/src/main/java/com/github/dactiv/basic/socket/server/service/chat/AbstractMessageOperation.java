package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.FileObject;
import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.WebDataBinder;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 抽象的消息操作实现，用于分离一下可复用的公共方法
 *
 * @author maurice.chen
 */
public abstract class AbstractMessageOperation implements MessageOperation {

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
     */
    protected void setGlobalMessageCurrentFilename(GlobalMessageMeta global,
                                                   String globalFilename,
                                                   Integer historyFileCount) {
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
    }

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

            BasicMessageMeta.FileMessage fileMessage = BasicMessageMeta.FileMessage.of(message, filename);
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
}
