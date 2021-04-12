package com.fuyu.basic.commons.enumerate.support;

import com.fuyu.basic.commons.enumerate.NameValueEnum;

/**
 * 是或否枚举
 *
 * @author maurice
 */
public enum YesOrNo implements NameValueEnum<Integer> {

    /**
     * 是
     */
    Yes(1, "是"),
    /**
     * 否
     */
    No(0, "否");

    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;

    /**
     * 是或否枚举
     *
     * @param value 值
     * @param name  名称
     */
    YesOrNo(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}

