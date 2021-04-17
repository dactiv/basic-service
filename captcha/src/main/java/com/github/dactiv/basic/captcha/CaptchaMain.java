package com.github.dactiv.basic.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableWebSecurity
@EnableFeignClients
@EnableDiscoveryClient
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.captcha")
public class CaptchaMain {

    public static void main(String[] args) {
        SpringApplication.run(CaptchaMain.class, args);
    }
}
