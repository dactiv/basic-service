package com.fuyu.basic.support.security.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 设备唯一识别的 spring security context 配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("spring.security.support.device-identified")
public class DeviceIdentifiedProperties {

    /**
     * 默认存储在 redis 的 security context key 名称
     */
    public final static String DEFAULT_SPRING_SECURITY_CONTEXT_KEY = "spring:security:context:";

    /**
     * 默认登陆处理 url
     */
    public final static String DEFAULT_LOGIN_PROCESSING_URL = "/login";

    /**
     * 安唯一识别存储于在 redis 的当前用户信息 key 前缀
     */
    private String springSecurityContextKey = DEFAULT_SPRING_SECURITY_CONTEXT_KEY;

    /**
     * 登陆 url
     */
    private String loginProcessingUrl = DEFAULT_LOGIN_PROCESSING_URL;

    /**
     * 是否允许 session 创建，默认为 true
     */
    private boolean allowSessionCreation = true;

    /**
     * 禁用 url 重写
     */
    private boolean disableUrlRewriting = false;

    public DeviceIdentifiedProperties() {
    }

    public String getSpringSecurityContextKey() {
        return springSecurityContextKey;
    }

    public void setSpringSecurityContextKey(String springSecurityContextKey) {
        this.springSecurityContextKey = springSecurityContextKey;
    }

    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    public boolean getAllowSessionCreation() {
        return allowSessionCreation;
    }

    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }

    public boolean getDisableUrlRewriting() {
        return disableUrlRewriting;
    }

    public void setDisableUrlRewriting(boolean disableUrlRewriting) {
        this.disableUrlRewriting = disableUrlRewriting;
    }
}
