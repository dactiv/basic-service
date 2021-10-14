package com.github.dactiv.basic.file.manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.commons.utils.MinioUtils;
import io.minio.MinioClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(ApplicationConfig.class)
public class ApplicationStartupAutoConfig implements InitializingBean {

    @Autowired
    private MinioClient minioClient;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * minio 客户端
     *
     * @param applicationConfig mini 模版
     *
     * @return mini 模版
     */
    @Bean
    public MinioClient minioClient(ApplicationConfig applicationConfig) {
        return MinioClient
                .builder()
                .endpoint(applicationConfig.getMinio().getEndpoint())
                .credentials(applicationConfig.getMinio().getAccessKey(), applicationConfig.getMinio().getSecretKey())
                .build();
    }

    @Override
    public void afterPropertiesSet() {
        MinioUtils.setMinioClient(minioClient);
        MinioUtils.setObjectMapper(objectMapper == null ? new ObjectMapper() : objectMapper);
    }
}
