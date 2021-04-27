package com.github.dactiv.basic.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableWebSecurity
@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.message")
public class MessageMain {

    public static void main(String[] args) {
        SpringApplication.run(MessageMain.class, args);
    }
}
