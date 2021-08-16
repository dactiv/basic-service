package com.github.dactiv.basic.authentication;

import com.github.dactiv.basic.authentication.service.security.CaptchaAuthenticationFilter;
import com.github.dactiv.basic.authentication.service.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.basic.authentication.service.security.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.basic.authentication.service.security.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.basic.authentication.service.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.basic.authentication.service.security.session.SessionControlAuthenticationStrategy;
import com.github.dactiv.framework.spring.security.SpringSecuritySupportAutoConfiguration;
import com.github.dactiv.framework.spring.security.authentication.RequestFormLoginConfiguration;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.AnonymousUserAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.provider.PrincipalAuthenticationProvider;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import java.util.List;

/**
 * spring security 配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SpringSecurityConfig<S extends Session> extends WebSecurityConfigurerAdapter {

    @Autowired
    private JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    @Autowired
    private JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Autowired
    private JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private PrincipalAuthenticationProvider principalAuthenticationProvider;

    @Autowired
    private AnonymousUserAuthenticationProvider anonymousUserAuthenticationProvider;

    @Autowired
    private SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry;

    @Autowired
    private SessionControlAuthenticationStrategy sessionControlAuthenticationStrategy;

    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {

        managerBuilder
                .authenticationProvider(daoAuthenticationProvider)
                .authenticationProvider(anonymousUserAuthenticationProvider)
                .authenticationProvider(principalAuthenticationProvider)
                .parentAuthenticationManager(principalAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .apply(new RequestFormLoginConfiguration<>(new CaptchaAuthenticationFilter()))
                .loginPage("/login")
                .successHandler(jsonAuthenticationSuccessHandler)
                .failureHandler(jsonAuthenticationFailureHandler)
                .and()
                .exceptionHandling() // FIXME 这一段有点费解。
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(jsonLogoutSuccessHandler)
                .and()
                .formLogin()
                .disable()
                //.httpBasic()
                //.and()
                .cors()
                .disable()
                .csrf()
                .disable()
                .requestCache()
                .disable()
                .securityContext()
                .securityContextRepository(securityContextRepository)
                .and()
                .sessionManagement()
                .sessionAuthenticationStrategy(sessionControlAuthenticationStrategy)
                /*.maximumSessions(2) // 限制登陆次数
                .sessionRegistry(springSessionBackedSessionRegistry)
                .expiredSessionStrategy(jsonSessionInformationExpiredStrategy)*/;

        SpringSecuritySupportAutoConfiguration.addConsensusBasedToMethodSecurityInterceptor(http);

    }

    @Bean
    public SessionControlAuthenticationStrategy sessionControlAuthenticationStrategy(SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry) {
        return new SessionControlAuthenticationStrategy(springSessionBackedSessionRegistry);

    }

    @Bean
    public SpringSessionBackedSessionRegistry<S> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<S> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean
    PrincipalAuthenticationProvider principalAuthenticationProvider(List<UserDetailsService> userDetailsServices,
                                                                    RedissonClient redissonClient) {
        return new PrincipalAuthenticationProvider(redissonClient, userDetailsServices);
    }

}
