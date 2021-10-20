package com.github.dactiv.basic.socket.server.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalMessageType implements NameValueEnum<Integer> {

    /**
     * 全局消息
     */
    Global(10, "全局消息"),
    /**
     * 联系人消息
     */
    Contact(20, "联系人消息");
    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;
}
