package com.fuyu.basic.support.security.enumerate;

import com.fuyu.basic.commons.enumerate.NameValueEnum;

/**
 * 用户状态
 *
 * @author maurice.chen
 */
public enum UserStatus implements NameValueEnum<Integer> {
    /**
     * 启用
     */
    Enabled(1, "启用"),

    /**
     * 禁用
     */
    Disabled(2, "禁用"),

    /**
     * 锁定
     */
    Lock(3, "锁定");

    /**
     * 用户状态
     *
     * @param value 值
     * @param name  名称
     */
    UserStatus(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    private String name;

    private Integer value;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
