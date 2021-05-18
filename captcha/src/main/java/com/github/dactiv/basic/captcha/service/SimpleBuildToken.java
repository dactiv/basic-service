package com.github.dactiv.basic.captcha.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 简单的验证码绑定 token 实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class SimpleBuildToken implements BuildToken {

    private static final long serialVersionUID = 3913898137626092376L;

    /**
     * 唯一识别
     */
    private String id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 绑定 token 值
     */
    private CacheProperties token;

    /**
     * 验证码类型
     */
    private String type;

    /**
     * 提交时的绑定 token 参数名称
     */
    private String paramName;

    /**
     * 构造验证码的参数信息
     */
    private Map<String, Object> args;

    /**
     * 拦截 token
     */
    @JsonIgnore
    private BuildToken interceptToken;

    @Override
    public boolean isExpired() {

        TimeProperties time = token.getExpiresTime();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expireTime = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(time.getValue(), time.getUnit().toChronoUnit());

        return now.isAfter(expireTime);
    }

}
