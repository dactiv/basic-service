package com.github.dactiv.basic.captcha.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 生成验证码结果集
 *
 * @author maurice
 */
@Data
@RequiredArgsConstructor
public class GenerateCaptchaResult {

    /**
     * 结果集
     */
    @NotNull
    private final Object result;

    /**
     * 生成账户名称
     */
    private String username;

    /**
     * 验证码值
     */
    @NotNull
    private final String captchaValue;

}
