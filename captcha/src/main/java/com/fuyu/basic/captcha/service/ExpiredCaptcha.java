package com.fuyu.basic.captcha.service;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 抽象的可过期验证码实现
 *
 * @author maurice
 */
public class ExpiredCaptcha implements Expired, Serializable {

    private static final long serialVersionUID = -2371567553401150929L;
    /**
     * 创建时间
     */
    private LocalDateTime creationTime;

    /**
     * 过期时间（单位：秒）
     */
    private Duration expireTime;

    /**
     * 验证码
     */
    private String captcha;
    /**
     * 使用验证码的账户名称
     */
    private String username;

    /**
     * 抽象的可过期验证码实现
     */
    public ExpiredCaptcha() {
        this.creationTime = LocalDateTime.now();
    }

    /**
     * 抽象的可过期验证码实现
     *
     * @param expireTime 过期时间（单位：秒）
     */
    public ExpiredCaptcha(Duration expireTime) {
        this.creationTime = LocalDateTime.now();
        this.expireTime = expireTime;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 获取过期时间（单位：秒）
     *
     * @return 过期时间
     */
    public Duration getExpireTime() {
        return expireTime;
    }

    /**
     * 设置过期时间（单位：秒）
     *
     * @param expireTime 过期时间（单位：秒）
     */
    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public boolean isExpired() {
        return !expireTime.isNegative() && LocalDateTime.now().minus(expireTime).isAfter(getCreationTime());
    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    public String getCaptcha() {
        return captcha;
    }

    /**
     * 设置验证码
     *
     * @param captcha 验证码
     */
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    /**
     * 设置使用验证码的账户名称
     *
     * @param username 使用验证码的账户名称
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取使用验证码的账户名称
     *
     * @return 使用验证码的账户名称
     */
    public String getUsername() {
        return username;
    }
}
