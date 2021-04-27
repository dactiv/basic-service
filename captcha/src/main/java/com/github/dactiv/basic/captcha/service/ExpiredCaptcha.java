package com.github.dactiv.basic.captcha.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 抽象的可过期验证码实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class ExpiredCaptcha implements Expired, Serializable {

    private static final long serialVersionUID = -2371567553401150929L;
    /**
     * 创建时间
     */
    private LocalDateTime creationTime = LocalDateTime.now();

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

    @Override
    public boolean isExpired() {
        return !expireTime.isNegative() && LocalDateTime.now().minus(expireTime).isAfter(getCreationTime());
    }

}
