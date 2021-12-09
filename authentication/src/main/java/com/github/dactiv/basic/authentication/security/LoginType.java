package com.github.dactiv.basic.authentication.security;

import com.github.dactiv.framework.commons.enumerate.NameEnum;

/**
 * 登录类型
 *
 * @author maurice
 */
public enum LoginType implements NameEnum {

    /**
     * 登录账户与密码登录
     */
    Username("登录账户与密码登录"),
    /**
     * 移动验证码登录
     */
    Mobile("移动验证码登录");

    LoginType(String name) {
        this.name = name;
    }

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}
