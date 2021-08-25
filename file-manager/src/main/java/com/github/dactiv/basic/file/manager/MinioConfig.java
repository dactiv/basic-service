package com.github.dactiv.basic.file.manager;

import io.minio.MinioClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * minio 配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(MinioConfig.MinioProperties.class)
public class MinioConfig {

    /**
     * mini 模版
     *
     * @param minioProperties mini 模版
     *
     * @return mini 模版
     */
    @Bean
    MinioClient minioTemplate(MinioConfig.MinioProperties minioProperties) {
        return  MinioClient
                .builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * minio 属性类
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @ConfigurationProperties("spring.minio")
    public static class MinioProperties {

        /**
         * 终端地址
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 安全密钥
         */
        private String secretKey;

        /**
         * 下载连接前缀
         */
        private String downloadPrefix = "localhost:9010/api/v1/buckets";

    }

}
