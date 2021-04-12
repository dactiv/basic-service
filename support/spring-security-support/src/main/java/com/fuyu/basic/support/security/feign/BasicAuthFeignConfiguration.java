package com.fuyu.basic.support.security.feign;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基础认证 feign 配置
 *
 * @author maurice
 */
@Configuration
public class BasicAuthFeignConfiguration {

    @Value("${spring.security.user.name:feign}")
    private String username;

    @Value("${spring.security.user.password:feign}")
    private String password;

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
