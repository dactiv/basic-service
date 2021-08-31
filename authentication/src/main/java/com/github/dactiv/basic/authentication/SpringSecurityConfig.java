package com.github.dactiv.basic.authentication;

import com.github.dactiv.basic.authentication.service.security.AuthenticationExtendProperties;
import com.github.dactiv.basic.authentication.service.security.CaptchaAuthenticationFilter;
import com.github.dactiv.basic.authentication.service.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.basic.authentication.service.security.handler.CaptchaAuthenticationFailureResponse;
import com.github.dactiv.basic.authentication.service.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.basic.authentication.service.security.session.SessionControlAuthenticationStrategy;
import com.github.dactiv.framework.spring.security.SpringSecuritySupportAutoConfiguration;
import com.github.dactiv.framework.spring.security.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
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

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@AutoConfigureAfter({SpringSecuritySupportAutoConfiguration.class, RedisHttpSessionConfiguration.class})
public class SpringSecurityConfig<S extends Session> implements WebSecurityConfigurerAfterAdapter {

    @Autowired
    private AuthenticationProperties properties;

    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Autowired
    private JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    @Autowired
    private JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    @Autowired
    private SessionControlAuthenticationStrategy sessionControlAuthenticationStrategy;

    @Autowired
    private AuthenticationExtendProperties extendProperties;

    @Autowired
    private SpringSessionBackedSessionRegistry<S> sessionBackedSessionRegistry;

    @Autowired
    private JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    @Autowired
    private CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {

        CaptchaAuthenticationFilter filter = new CaptchaAuthenticationFilter(
                properties,
                captchaAuthenticationFailureResponse
        );

        filter.setAuthenticationManager(authenticationManager);
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);

        httpSecurity
                .logout()
                .logoutUrl(extendProperties.getLogoutUrl())
                .logoutSuccessHandler(jsonLogoutSuccessHandler)
                .and()
                .addFilter(filter)
                .sessionManagement()
                .sessionAuthenticationStrategy(sessionControlAuthenticationStrategy)
                .maximumSessions(Integer.MAX_VALUE)
                .sessionRegistry(sessionBackedSessionRegistry)
                .expiredSessionStrategy(jsonSessionInformationExpiredStrategy);
    }

    @Bean
    public SessionControlAuthenticationStrategy sessionControlAuthenticationStrategy(SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry) {
        return new SessionControlAuthenticationStrategy(springSessionBackedSessionRegistry);
    }

    @Bean
    public SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<S> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

}
