package com.fuyu.basic.authentication.service.security;

import com.fuyu.basic.commons.enumerate.NameEnum;

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

    private String name;

    @Override
    public String getName() {
        return name;
    }
}
