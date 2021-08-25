package com.github.dactiv.basic.file.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 服务启动类
 *
 * @author maurice
 */
@EnableWebSecurity
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.file.manager")
public class FileManagerMain {

    public static void main(String[] args) {
        SpringApplication.run(FileManagerMain.class, args);
    }
}
