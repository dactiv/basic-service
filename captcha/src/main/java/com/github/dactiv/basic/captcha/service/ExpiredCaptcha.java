package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 抽象的可过期验证码实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class ExpiredCaptcha implements Expired, Serializable {

    private static final long serialVersionUID = 2371567553401150929L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 过期时间（单位：秒）
     */
    private TimeProperties expireTime;

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

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expire = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(expireTime.getValue(), expireTime.getUnit().toChronoUnit());

        return now.isAfter(expire);
    }

}
