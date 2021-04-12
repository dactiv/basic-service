package com.fuyu.basic.support.security.authentication;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * spring boot 当前用户 from 登陆配置类
 *
 * @author maurice.chen
 */
public class RequestFormLoginConfiguration<H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, RequestFormLoginConfiguration<H>, RequestAuthenticationFilter> {

    private static final String DEFAULT_LOGIN_PROCESSING_URL = "/login";

    /**
     * spring boot 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration() {
        super(new RequestAuthenticationFilter(), DEFAULT_LOGIN_PROCESSING_URL);
    }

    /**
     * spring boot 当前用户 from 登陆配置类
     *
     * @param authenticationFilter 认证 filter
     */
    public RequestFormLoginConfiguration(RequestAuthenticationFilter authenticationFilter) {
        super(authenticationFilter, DEFAULT_LOGIN_PROCESSING_URL);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    @Override
    public RequestFormLoginConfiguration<H> loginPage(String loginPage) {
        return super.loginPage(loginPage);
    }

    /**
     * 登陆账户参数名
     *
     * @param usernameParameter 登陆账户参数名
     * @return 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration<H> usernameParameter(String usernameParameter) {
        getAuthenticationFilter().setUsernameParameter(usernameParameter);
        return this;
    }

    /**
     * 登陆账户密码参数名
     *
     * @param passwordParameter 登陆账户密码参数名
     * @return 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration<H> passwordParameter(String passwordParameter) {
        getAuthenticationFilter().setPasswordParameter(passwordParameter);
        return this;
    }

    /**
     * 用户类型 header 名
     *
     * @param typeHeaderName 用户类型 header 名
     * @return 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration<H> typeHeaderName(String typeHeaderName) {
        getAuthenticationFilter().setTypeHeaderName(typeHeaderName);
        return this;
    }

    /**
     * 认证失败 forward url
     *
     * @param forwardUrl 认证失败 forward url
     * @return 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration<H> failureForwardUrl(String forwardUrl) {
        failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
        return this;
    }

    /**
     * 认证成功 forward url
     *
     * @param forwardUrl 认证成功 forward url
     * @return 当前用户 from 登陆配置类
     */
    public RequestFormLoginConfiguration<H> successForwardUrl(String forwardUrl) {
        successHandler(new ForwardAuthenticationSuccessHandler(forwardUrl));
        return this;
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
    }

}
