package com.fuyu.basic.commons.enumerate.support;

import com.fuyu.basic.commons.enumerate.NameValueEnum;

/**
 * 执行状态枚举
 *
 * @author maurice
 */
public enum ExecuteStatus implements NameValueEnum<Integer> {

    /**
     * 执行中
     */
    Processing("执行中", 0),

    /**
     * 执行成功
     */
    Success("执行成功", 1),

    /**
     * 执行失败
     */
    Failure("执行失败", 99);

    /**
     * 执行状态枚举
     *
     * @param name  名称
     * @param value 值
     */
    ExecuteStatus(String name, Integer value) {
        this.name = name;
        this.value = value;
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
