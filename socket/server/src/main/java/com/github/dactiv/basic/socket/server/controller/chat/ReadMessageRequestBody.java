package com.github.dactiv.basic.socket.server.controller.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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
    private Date creationTime = new Date();

    /**
     * 发送者用户 id
     */
    private Integer senderId;

    /**
     * 收信者用户 id
     */
    private Integer recipientId;

    /**
     * 消息实体 key 为消息存在的文件，value 为消息的 id
     */
    private Map<String, String> messages;
}
