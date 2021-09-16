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
@ConditionalOnProperty(prefix = "dactiv.socket.client", value = "enabled", matchIfMissing = true)
public class SocketClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SocketClientTemplate.class)
    public SocketClientTemplate socketClientTemplate(RestTemplate restTemplate,
                                                                                    DiscoveryClient discoveryClient,
                                                                                    ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                                                                    SecurityProperties securityProperties) {
        return com.github.dactiv.basic.socket.client.SocketClientTemplate.of(discoveryClient, restTemplate, threadPoolTaskExecutor, securityProperties);
    }

    @Bean
    public SocketResultResponseBodyAdvice socketResultResponseBodyAdvice(SpringWebSupportProperties properties,
                                                                         SocketClientTemplate socketClientTemplate) {
        return new SocketResultResponseBodyAdvice(properties, socketClientTemplate);
    }

    @Bean
    public SocketMessageInterceptor socketMessageInterceptor(SocketClientTemplate socketClientTemplate) {
        return new SocketMessageInterceptor(socketClientTemplate);
    }

    @Bean
    public SocketMessagePointcutAdvisor socketMessagePointcutAdvisor(SocketMessageInterceptor socketMessageInterceptor) {
        return new SocketMessagePointcutAdvisor(socketMessageInterceptor);
    }
}
