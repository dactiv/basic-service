package com.github.dactiv.basic.captcha.service;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 抽象的可重试的验证码实现
 *
 * @author maurice
 */
public class ReusableCaptcha extends ExpiredCaptcha implements Serializable, Reusable {

    private static final long serialVersionUID = -2295130548867148592L;
    /**
     * 重试时间（单位：秒）
     */
    private Duration retryTime;

    /**
     * 抽象的可重试的验证码实现
     */
    public ReusableCaptcha() {
        super();
    }

    /**
     * 抽象的可重试的验证码实现
     *
     * @param expireTime 过期时间（单位：秒）
     * @param retryTime  重试时间（单位：秒）
     */
    public ReusableCaptcha(Duration expireTime, Duration retryTime) {
        super(expireTime);
        this.retryTime = retryTime;
    }

    /**
     * 获取重试时间（单位：秒）
     *
     * @return 重试时间（单位：秒）
     */
    public Duration getRetryTime() {
        return retryTime;
    }

    /**
     * 设置重试时间（单位：秒）
     *
     * @param retryTime 重试时间（单位：秒）
     */
    public void setRetryTime(Duration retryTime) {
        this.retryTime = retryTime;
    }

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    @Override
    public boolean isRetry() {
        return !retryTime.isNegative() && LocalDateTime.now().minus(retryTime).isAfter(getCreationTime());
    }
}
