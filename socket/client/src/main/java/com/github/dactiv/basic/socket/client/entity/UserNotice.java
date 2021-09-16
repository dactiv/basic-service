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

    public static final String RABBIT_MQ_USER_SOCKET_FANOUT_EXCHANGE_NAME = "game.sg.socket.user.exchange";

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
    private Integer type;

}
