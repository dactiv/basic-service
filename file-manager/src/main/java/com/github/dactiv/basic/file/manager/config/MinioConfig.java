package com.github.dactiv.basic.file.manager.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * minio 配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(ApplicationConfig.class)
public class MinioConfig {

    /**
     * mini 模版
     *
     * @param applicationConfig mini 模版
     *
     * @return mini 模版
     */
    @Bean
    MinioClient minioTemplate(ApplicationConfig applicationConfig) {
        return MinioClient
                .builder()
                .endpoint(applicationConfig.getEndpoint())
                .credentials(applicationConfig.getAccessKey(), applicationConfig.getSecretKey())
                .build();
    }

}
