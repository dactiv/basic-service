package com.fuyu.basic.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuyu.basic.support.crypto.access.CryptoAlgorithm;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.fuyu.basic.gateway")
public class GatewayMain {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class, args);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RestResultGatewayBlockExceptionHandler restResultGatewayBlockExceptionHandler(ObjectMapper objectMapper) {
        return new RestResultGatewayBlockExceptionHandler(objectMapper);
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
    @ConfigurationProperties("spring.application.crypto.access.algorithm-mode")
    public CryptoAlgorithm accessTokenAlgorithm() {
        return new CryptoAlgorithm();
    }
}