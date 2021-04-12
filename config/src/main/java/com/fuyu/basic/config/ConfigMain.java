package com.fuyu.basic.config;

import com.fuyu.basic.support.crypto.access.CryptoAlgorithm;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.client.RestTemplate;


/**
 * 服务启动类
 *
 * @author maurice
 */
@EnableWebSecurity
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SpringBootApplication(scanBasePackages = "com.fuyu.basic.config")
public class ConfigMain {

    public static void main(String[] args) {
        SpringApplication.run(ConfigMain.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.application.crypto.access.algorithm-mode")
    public CryptoAlgorithm accessTokenAlgorithm() {
        return new CryptoAlgorithm();
    }
}
