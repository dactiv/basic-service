package com.github.dactiv.basic.socket.server.domain.body.request;

import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 读取消息 request body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ReadMessageRequestBody implements Serializable {

    private static final long serialVersionUID = 188961857092699568L;

    /**
     * 创建时间
     */
    private Date creationTime;

    /**
     * 发送者用户 id
     */
    private Integer senderId;

    /**
     * 目标类型
     */
    private MessageTypeEnum type;

    /**
     * 收信者用户 id
     */
    private Integer recipientId;

    /**
     * 消息 id 集合
     */
    private List<String> messageIds;
}
