package com.github.dactiv.basic.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型
 *
 * @author maurice
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoginTypeEnum implements NameEnum {

    /**
     * 登录账户与密码登录
     */
    Username("登录账户与密码登录"),
    /**
     * 移动验证码登录
     */
    Mobile("移动验证码登录");

    private final String name;
}
