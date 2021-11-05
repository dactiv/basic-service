package com.github.dactiv.basic.socket.server.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 联系人类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContactType implements NameValueEnum<Integer> {

    /**
     * 参与者
     */
    Person(10, "人员"),

    /**
     * 拥有者
     */
    Group(20, "群组");

    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;
}
