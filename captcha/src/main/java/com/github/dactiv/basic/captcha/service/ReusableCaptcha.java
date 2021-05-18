package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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

    private static final long serialVersionUID = 2295130548867148592L;

    /**
     * 重试时间（单位：秒）
     */
    private TimeProperties retryTime;

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    @Override
    public boolean isRetry() {

        TimeProperties time = retryTime;

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expireTime = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(time.getValue(), time.getUnit().toChronoUnit());

        return now.isAfter(expireTime);
    }
}
