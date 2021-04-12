package com.fuyu.basic.captcha.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 简单的验证码绑定 token 实现
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SimpleBuildToken implements BuildToken {

    private static final long serialVersionUID = -3913898137626092376L;

    private final LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 唯一识别
     */
    private String id;
    /**
     * 绑定 token 值
     */
    private String token;
    /**
     * 验证码类型
     */
    private String type;
    /**
     * 提交时的绑定 token 参数名称
     */
    private String paramName;
    /**
     * 活期时间
     */
    private Duration expireTime;
    /**
     * 构造验证码的参数信息
     */
    private Map<String, Object> args;
    /**
     * 拦截 token
     */
    private BuildToken interceptToken;

    /**
     * 简单的验证码绑定 token 实现
     */
    public SimpleBuildToken() {
    }

    /**
     * 简单的验证码绑定 token 实现
     *
     * @param token     token 值
     * @param type      验证码类型
     * @param paramName 提交时的绑定 token 参数名称
     * @param args      构造参数信息
     */
    public SimpleBuildToken(String id, String token, String type, String paramName, Map<String, Object> args) {
        this.id = id;
        this.token = token;
        this.type = type;
        this.paramName = paramName;
        this.args = args;
    }

    @Override
    public String getToken() {
        return token;
    }

    /**
     * 设置绑定 token 值
     *
     * @param token 绑定 token 值
     */
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * 设置验证码类型
     *
     * @param type 验证码类型
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getParamsName() {
        return paramName;
    }

    /**
     * 设置提交时的绑定 token 参数名称
     *
     * @param paramName 提交时的绑定 token 参数名称
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public Map<String, Object> getArgs() {
        return args;
    }

    /**
     * 设置构造验证码的参数信息
     *
     * @param args 构造验证码的参数信息
     */
    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    @Override
    @JsonIgnore
    public Duration getExpireTime() {
        return expireTime;
    }

    @Override
    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    @JsonIgnore
    public void setInterceptToken(BuildToken token) {
        this.interceptToken = token;
    }

    @Override
    public BuildToken getInterceptToken() {
        return interceptToken;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 设置唯一识别
     *
     * @param id 唯一识别
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isExpired() {
        return !expireTime.isNegative() && LocalDateTime.now().minus(expireTime).isAfter(creationTime);
    }
}
