package com.github.dactiv.basic.socket.server.enitty;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.socket.server.enitty.Room;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 联系人实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Contact implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 6466725003068761373L;

    public static final String DEFAULT_MESSAGE_IDS = "messageIds";

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 最后发送消息时间
     */
    private Date lastSendTime = new Date();

    /**
     * 消息内容
     */
    private List<Message> messages = new ArrayList<>();

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
        @JsonIgnore
        private String cryptoKey;

        /**
         * 密钥类型
         */
        @JsonIgnore
        private String cryptoType;

    }
}
