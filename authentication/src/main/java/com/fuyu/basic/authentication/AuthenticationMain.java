package com.fuyu.basic.authentication;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.client.RestTemplate;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableWebSecurity
@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisHttpSession
@SpringBootApplication(scanBasePackages = "com.fuyu.basic.authentication")
public class AuthenticationMain {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationMain.class, args);
    }
}
