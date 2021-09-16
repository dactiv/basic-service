package com.github.dactiv.basic.socket.client;

import com.github.dactiv.basic.socket.client.holder.Interceptor.SocketMessageInterceptor;
import com.github.dactiv.basic.socket.client.holder.SocketMessagePointcutAdvisor;
import com.github.dactiv.framework.spring.web.SpringWebMvcSupportAutoConfiguration;
import com.github.dactiv.framework.spring.web.SpringWebSupportProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * socket 客户端自动配置累
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(SpringWebMvcSupportAutoConfiguration.class)
@EnableConfigurationProperties({SpringWebSupportProperties.class, SecurityProperties.class})
@ConditionalOnProperty(prefix = "sg.socket.client", value = "enabled", matchIfMissing = true)
public class SocketClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(com.github.dactiv.basic.socket.client.SocketClientTemplate.class)
    public com.github.dactiv.basic.socket.client.SocketClientTemplate socketClientTemplate(RestTemplate restTemplate,
                                                                                    DiscoveryClient discoveryClient,
                                                                                    ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                                                                    SecurityProperties securityProperties) {
        return com.github.dactiv.basic.socket.client.SocketClientTemplate.of(discoveryClient, restTemplate, threadPoolTaskExecutor, securityProperties);
    }

    @Bean
    public com.github.dactiv.basic.socket.client.SocketResultResponseBodyAdvice socketResultResponseBodyAdvice(SpringWebSupportProperties properties,
                                                                                                        com.github.dactiv.basic.socket.client.SocketClientTemplate socketClientTemplate) {
        return new com.github.dactiv.basic.socket.client.SocketResultResponseBodyAdvice(properties, socketClientTemplate);
    }

    @Bean
    public SocketMessageInterceptor socketMessageInterceptor(com.github.dactiv.basic.socket.client.SocketClientTemplate socketClientTemplate) {
        return new SocketMessageInterceptor(socketClientTemplate);
    }

    @Bean
    public SocketMessagePointcutAdvisor socketMessagePointcutAdvisor(SocketMessageInterceptor socketMessageInterceptor) {
        return new SocketMessagePointcutAdvisor(socketMessageInterceptor);
    }
}