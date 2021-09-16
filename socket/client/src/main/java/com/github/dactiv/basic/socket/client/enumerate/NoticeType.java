package com.github.dactiv.basic.socket.client.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户通知类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NoticeType implements NameValueEnum<Integer> {

    /**
     * 系统通知
     */
    System(10, "系统");

    /**
     * 默认系统类型的用户通知事件名称
     */
    public static final String SYSTEM_USER_NOTICE_EVENT_NAME = "SYSTEM_USER_NOTICE_EVENT";

    private final Integer value;

    private final String name;
}
