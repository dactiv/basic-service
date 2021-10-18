package com.github.dactiv.basic.socket.server.service.chat.data;

import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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

        /**
         * 消息存在的文件名称
         */
        private String filename;

    }
}
