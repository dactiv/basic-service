package com.github.dactiv.basic.captcha.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 抽象的可重试的验证码实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReusableCaptcha extends ExpiredCaptcha implements Serializable, Reusable {

    private static final long serialVersionUID = -2295130548867148592L;

    /**
     * 重试时间（单位：秒）
     */
    private Duration retryDuration;

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    @Override
    public boolean isRetry() {

        LocalDateTime retryTime = LocalDateTime.now().minus(retryDuration);
        LocalDateTime creationTime = LocalDateTime.ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault());

        return !retryDuration.isNegative() && retryTime.isAfter(creationTime);
    }
}
