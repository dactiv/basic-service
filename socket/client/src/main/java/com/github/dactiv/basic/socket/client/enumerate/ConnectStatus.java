package com.github.dactiv.basic.socket.client.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 连接状态
 *
 * @author maurice
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConnectStatus implements NameValueEnum<Integer> {

    /**
     * 断开链接
     */
    Disconnected("断开", 10),
    /**
     * 链接中
     */
    Connecting("连接中", 20),
    /**
     * 已链接
     */
    Connect("已链接", 30);

    private final String name;

    private final Integer value;
}
