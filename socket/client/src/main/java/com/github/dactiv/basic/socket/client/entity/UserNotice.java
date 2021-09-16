package com.github.dactiv.basic.socket.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户通知
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class UserNotice {

    public static final String SYSTEM_USER_NOTICE_EVENT_NAME = "SYSTEM_USER_NOTICE_EVENT";

    /**
     * 标题
     */
    private String title;

    /**
     * 消息
     */
    private String message;

    /**
     * 类型
     */
    private String type;

}
