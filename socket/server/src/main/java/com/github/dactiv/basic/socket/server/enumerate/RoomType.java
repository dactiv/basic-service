package com.github.dactiv.basic.socket.server.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RoomType implements NameValueEnum<Integer> {

    /**
     * 群聊
     */
    Group(10, "群聊"),
    /**
     * 点对点聊
     */
    P2P(20, "点对点聊");

    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;

}
