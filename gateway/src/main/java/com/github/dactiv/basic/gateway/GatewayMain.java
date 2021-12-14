package com.github.dactiv.basic.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.gateway.config.ApplicationConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.stream.Collectors;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients("com.github.dactiv.basic.commons.feign")
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.gateway")
public class GatewayMain {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class, args);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RestResultGatewayBlockExceptionHandler restResultGatewayBlockExceptionHandler(ObjectMapper objectMapper,
                                                                                         ApplicationConfig applicationConfig) {
        return new RestResultGatewayBlockExceptionHandler(objectMapper, applicationConfig);
    }

    @Bean
    public HttpMessageConverters httpMessageConverters(ObjectProvider<HttpMessageConverter<?>> messageConverters) {
        return new HttpMessageConverters(messageConverters.orderedStream().collect(Collectors.toList()));
    }

}