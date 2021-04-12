package com.fuyu.basic.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuyu.basic.support.security.asscess.UserTypeVoter;
import com.fuyu.basic.support.security.audit.ControllerAuditHandlerInterceptor;
import com.fuyu.basic.support.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import com.fuyu.basic.support.security.authentication.DeviceIdentifiedProperties;
import com.fuyu.basic.support.security.authentication.DeviceIdentifiedSecurityContextRepository;
import com.fuyu.basic.support.security.authentication.provider.AnonymousUserAuthenticationProvider;
import com.fuyu.basic.support.security.concurrent.ConcurrentInterceptor;
import com.fuyu.basic.support.security.concurrent.ConcurrentPointcutAdvisor;
import com.fuyu.basic.support.security.entity.AnonymousUser;
import com.fuyu.basic.support.security.entity.RoleAuthority;
import com.fuyu.basic.support.security.plugin.PluginEndpoint;
import com.fuyu.basic.support.security.version.AccessVersionControlHandlerInterceptor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security 重写支持自动配置类
 *
 * @author maurice
 */
@Configuration
@EnableConfigurationProperties(DeviceIdentifiedProperties.class)
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.security.support", value = "enabled", matchIfMissing = true)
public class SpringSecuritySupportAutoConfiguration {

    @Autowired(required = false)
    private List<InfoContributor> infoContributors;

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class DefaultWebMvcConfigurer implements WebMvcConfigurer {

        @Autowired
        private ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor;

        @Autowired
        private AccessVersionControlHandlerInterceptor accessVersionControlHandlerInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(controllerAuditHandlerInterceptor);
            registry.addInterceptor(accessVersionControlHandlerInterceptor);
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "spring.security.support", value = "override-http-security", matchIfMissing = true)
    static class DefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private DeviceIdentifiedSecurityContextRepository deviceIdentifiedSecurityContextRepository;

        @Autowired
        private AnonymousUserAuthenticationProvider anonymousUserAuthenticationProvider;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(anonymousUserAuthenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.authorizeRequests()
                    .anyRequest().permitAll()
                    .and().formLogin().disable().logout().disable()
                    .httpBasic().and()
                    .cors().disable()
                    .csrf().disable()
                    .requestCache().disable()
                    .securityContext()
                    .securityContextRepository(deviceIdentifiedSecurityContextRepository);

            addConsensusBasedToMethodSecurityInterceptor(http);

        }

    }

    /**
     * 添加 ConsensusBased 访问管理器到方法拦截器中
     *
     * @param http http security
     */
    public static void addConsensusBasedToMethodSecurityInterceptor(HttpSecurity http) {
        try {
            MethodSecurityInterceptor methodSecurityInterceptor = http
                    .getSharedObject(ApplicationContext.class)
                    .getBean(MethodSecurityInterceptor.class);

            AccessDecisionManager accessDecisionManager = methodSecurityInterceptor.getAccessDecisionManager();

            if (AbstractAccessDecisionManager.class.isAssignableFrom(accessDecisionManager.getClass())) {

                AbstractAccessDecisionManager adm = (AbstractAccessDecisionManager) accessDecisionManager;
                adm.getDecisionVoters().add(new UserTypeVoter());

                ConsensusBased consensusBased = new ConsensusBased(adm.getDecisionVoters());
                consensusBased.setAllowIfEqualGrantedDeniedDecisions(false);

                methodSecurityInterceptor.setAccessDecisionManager(consensusBased);
            }
        } catch (Exception ignored) {

        }
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    UserTypeVoter userTypeVoter() {
        return new UserTypeVoter();
    }

    @Bean
    @ConfigurationProperties("plugin")
    PluginEndpoint pluginEndpoint() {
        return new PluginEndpoint(infoContributors);
    }

    @Bean
    ElasticsearchAuditEventRepository elasticsearchAuditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                                                        SecurityProperties securityProperties) {
        return new ElasticsearchAuditEventRepository(elasticsearchRestTemplate, securityProperties);
    }


    @Bean
    ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor() {
        return new ControllerAuditHandlerInterceptor();
    }

    @Bean
    AccessVersionControlHandlerInterceptor accessVersionControlHandlerInterceptor(ObjectMapper objectMapper) {
        return new AccessVersionControlHandlerInterceptor(objectMapper);
    }

    @Bean
    DeviceIdentifiedSecurityContextRepository deviceIdentifiedSecurityContextRepository(
            RedisTemplate<String, Object> redisTemplate,
            DeviceIdentifiedProperties deviceIdentifiedProperties) {

        DeviceIdentifiedSecurityContextRepository repository = new DeviceIdentifiedSecurityContextRepository(redisTemplate);

        repository.setSpringSecurityContextKey(deviceIdentifiedProperties.getSpringSecurityContextKey());
        repository.setLoginProcessingUrl(deviceIdentifiedProperties.getLoginProcessingUrl());
        repository.setAllowSessionCreation(deviceIdentifiedProperties.getAllowSessionCreation());
        repository.setDisableUrlRewriting(deviceIdentifiedProperties.getDisableUrlRewriting());

        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties securityProperties,
                                                          PasswordEncoder passwordEncoder) {

        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        SecurityProperties.User user = securityProperties.getUser();

        inMemoryUserDetailsManager.createUser(
                new AnonymousUser(
                        user.getName(),
                        passwordEncoder.encode(user.getPassword()),
                        user.getPassword(),
                        user.getRoles()
                                .stream()
                                .map(r -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + r))
                                .collect(Collectors.toList())
                )
        );

        return inMemoryUserDetailsManager;
    }

    @Bean
    AnonymousUserAuthenticationProvider anonymousUserAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                                            SecurityProperties securityProperties,
                                                                            RedisTemplate<String, Object> redisTemplate) {

        AnonymousUserAuthenticationProvider authenticationProvider = new AnonymousUserAuthenticationProvider(redisTemplate);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        SecurityProperties.User user = securityProperties.getUser();

        authenticationProvider.createUser(new AnonymousUser(
                user.getName(),
                passwordEncoder.encode(user.getPassword()),
                user.getPassword(),
                user.getRoles()
                        .stream()
                        .map(r -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + r))
                        .collect(Collectors.toList())
        ));

        return authenticationProvider;
    }

    @Bean
    @ConditionalOnMissingBean(ConcurrentPointcutAdvisor.class)
    ConcurrentPointcutAdvisor concurrentPointcutAdvisor(RedissonClient redissonClient) {
        return new ConcurrentPointcutAdvisor(new ConcurrentInterceptor(redissonClient));
    }
}
