package com.github.dactiv.basic.socket.server.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 联系人消息
     */
    PERSON(10, "联系人消息"),
    /**
     * 联系人消息
     */
    GROUP(20, "群聊消息"),
    /**
     * 全局消息
     */
    GLOBAL(30, "全局消息");
    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;
}
