package com.github.dactiv.basic.socket.server.domain.meta;

import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * 全局消息基类
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class BasicMessageMeta implements Serializable {

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
         * 消息类型
         */
        private MessageTypeEnum type;

        /**
         * 密钥值
         */
        @Exclude(SystemConstants.CHAT_FILTER_RESULT_ID)
        private String cryptoKey;

        /**
         * 密钥类型
         */
        @Exclude(SystemConstants.CHAT_FILTER_RESULT_ID)
        private String cryptoType;

    }

    /**
     * 带文件名称的消息实体
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FileMessage extends Message {

        private static final long serialVersionUID = 1156805823503290540L;
        /**
         * 消息存在的文件名称
         */
        private String filename;

        /**
         * 创建带文件名称的消息实体
         *
         * @param message  消息实体
         * @param filename 存储的文件名称
         *
         * @return 带文件名称的消息实体
         */
        public static FileMessage of(Message message, String filename) {
            FileMessage result = Casts.of(message, FileMessage.class);
            result.setFilename(filename);
            return result;
        }
    }

    /**
     * 联系人的可读取消息实体
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ContactReadableMessage extends FileMessage {

        private static final long serialVersionUID = -7530473279126004040L;
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
     * 群聊的可读取消息实体
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GroupReadableMessage extends FileMessage {

        private static final long serialVersionUID = -7932031035796113862L;

        /**
         * 已读信息
         */
        private List<IntegerIdEntity> readerInfo = new LinkedList<>();
    }

    /**
     * 消息关联文件的实体，用于通过消息 id 快速定位所在的文件使用，在将消息设置为已读时用到。
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FileLinkMessage extends Message {

        private static final long serialVersionUID = -8782946827274914400L;
        /**
         * 消息存储在对应文件的名称
         */
        private List<String> filenames = new LinkedList<>();
        /**
         * 负载信息
         */
        private Map<String, Object> payload = new LinkedHashMap<>();
    }
}
