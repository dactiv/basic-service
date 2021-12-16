package com.github.dactiv.basic.authentication.config;

import com.github.dactiv.basic.authentication.security.CaptchaAuthenticationFilter;
import com.github.dactiv.basic.authentication.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.basic.authentication.security.handler.CaptchaAuthenticationFailureResponse;
import com.github.dactiv.basic.authentication.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.framework.spring.security.SpringSecurityAutoConfiguration;
import com.github.dactiv.framework.spring.security.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@AutoConfigureAfter({SpringSecurityAutoConfiguration.class, RedisHttpSessionConfiguration.class})
public class SpringSecurityConfig<S extends Session> implements WebSecurityConfigurerAfterAdapter {

    private final AuthenticationProperties properties;

    private final CookieRememberService rememberMeServices;

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    private final JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    private final ApplicationConfig applicationConfig;

    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    private final CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse;

    private final ApplicationEventPublisher eventPublisher;

    private final AuthenticationManager authenticationManager;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private SpringSessionBackedSessionRegistry<S> sessionBackedSessionRegistry;

    public SpringSecurityConfig(AuthenticationProperties properties,
                                CookieRememberService rememberMeServices,
                                JsonLogoutSuccessHandler jsonLogoutSuccessHandler,
                                JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy,
                                ApplicationConfig applicationConfig,
                                JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse,
                                ApplicationEventPublisher eventPublisher,
                                AuthenticationManager authenticationManager,
                                ObjectProvider<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver) {

        this.properties = properties;
        this.rememberMeServices = rememberMeServices;
        this.jsonLogoutSuccessHandler = jsonLogoutSuccessHandler;
        this.jsonAuthenticationSuccessHandler = jsonAuthenticationSuccessHandler;
        this.jsonSessionInformationExpiredStrategy = jsonSessionInformationExpiredStrategy;
        this.applicationConfig = applicationConfig;
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.captchaAuthenticationFailureResponse = captchaAuthenticationFailureResponse;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver.orderedStream().collect(Collectors.toList());
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {

        CaptchaAuthenticationFilter filter = new CaptchaAuthenticationFilter(
                properties,
                authenticationTypeTokenResolvers,
                captchaAuthenticationFailureResponse
        );

        filter.setAuthenticationManager(authenticationManager);
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);
        filter.setRememberMeServices(rememberMeServices);

        httpSecurity
                .logout()
                .logoutUrl(applicationConfig.getLogoutUrl())
                .logoutSuccessHandler(jsonLogoutSuccessHandler)
                .and()
                .addFilter(filter)
                .sessionManagement()
                .maximumSessions(Integer.MAX_VALUE)
                .sessionRegistry(sessionBackedSessionRegistry)
                .expiredSessionStrategy(jsonSessionInformationExpiredStrategy);
    }

    @Bean
    public SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<S> sessionRepository) {
        sessionBackedSessionRegistry = new SpringSessionBackedSessionRegistry<>(sessionRepository);
        return sessionBackedSessionRegistry;
    }

}
