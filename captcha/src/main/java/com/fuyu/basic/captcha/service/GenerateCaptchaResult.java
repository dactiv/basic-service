package com.fuyu.basic.captcha.service;

/**
 * 生成验证码结果集
 *
 * @author maurice
 */
public class GenerateCaptchaResult {

    /**
     * 结果集
     */
    private final Object result;

    /**
     * 生成账户名称
     */
    private String username;

    /**
     * 验证码值
     */
    private final String captchaValue;

    /**
     * 生成验证码结果集
     *
     * @param result       结果集
     * @param captchaValue 验证码值
     */
    public GenerateCaptchaResult(Object result, String captchaValue) {
        this.result = result;
        this.captchaValue = captchaValue;
    }

    /**
     * 获取结果集
     *
     * @return 结果集
     */
    public Object getResult() {
        return result;
    }

    /**
     * 获取验证码值
     *
     * @return 验证码值
     */
    public String getCaptchaValue() {
        return captchaValue;
    }

    /**
     * 获取生成账户名称
     *
     * @return 生成账户名称
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置生成账户名称
     *
     * @param username 生成账户名称
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
