package com.github.dactiv.basic.authentication;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
@MapperScan(basePackages = "com.github.dactiv.basic.authentication.dao")
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.authentication")
public class AuthenticationMain {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationMain.class, args);
    }

}
