package com.github.dactiv.basic.socket.server.service.chat;

import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.minio.MinioTemplate;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.WebDataBinder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 抽象的消息解析实现，用于分离一下可复用的公共方法
 *
 * @author maurice.chen
 */
public abstract class AbstractMessageResolver implements MessageResolver{

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
}
