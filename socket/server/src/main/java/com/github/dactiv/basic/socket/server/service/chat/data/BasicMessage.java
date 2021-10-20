package com.github.dactiv.basic.socket.server.service.chat.data;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 全局消息基类
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class BasicMessage implements Serializable {

    private static final long serialVersionUID = 7397284325411371767L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 最后发送消息时间
     */
    private Date lastSendTime = new Date();

    /**
     * 最后一条信息内容
     */
    private String lastMessage;

    /**
     * 消息实体，id 为发送者 id
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Message extends IdEntity<String> {

        private static final long serialVersionUID = -5472505273196782177L;

        /**
         * 创建时间
         */
        private Date creationTime = new Date();

        /**
         * 内容
         */
        private String content;

        /**
         * 发送者 id
         */
        private Integer senderId;

        /**
         * 密钥值
         */
        @Exclude("web")
        private String cryptoKey;

        /**
         * 密钥类型
         */
        @Exclude("web")
        private String cryptoType;

        /**
         * 是否已读
         */
        private boolean read = false;

        /**
         * 已读时间
         */
        private Date readTime;

    }

    /**
     * 带文件名的消息实体，用于存在用户消息时使用
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FileMessage extends Message {

        private static final long serialVersionUID = -4084857751977048367L;

        /**
         * 消息存在的文件名称
         */
        private String filename;

        /**
         * 创建带文件名的消息实体
         *
         * @param message  消息实体
         * @param filename 存储的文件名称
         *
         * @return 带文件名的消息实体
         */
        public static FileMessage of(Message message, String filename) {
            FileMessage userMessage = Casts.of(message, FileMessage.class);
            userMessage.setFilename(filename);
            return userMessage;
        }
    }

    /**
     * 响应给用户的消息实体
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserMessageBody extends Message {

        private static final long serialVersionUID = -8782946827274914400L;
        /**
         * 消息存在的文件名称
         */
        private List<String> filenames = new LinkedList<>();
    }
}
