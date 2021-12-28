package com.github.dactiv.basic.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author chenxiaobo
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GenderEnum implements NameValueEnum<Integer> {

    /**
     * 男
     */
    MALE(10, "男"),

    /**
     * 女
     */
    FEMALE(20, "女");

    private final Integer value;

    private final String name;
}
