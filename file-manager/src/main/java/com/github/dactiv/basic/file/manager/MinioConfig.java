package com.github.dactiv.basic.file.manager;

import com.github.dactiv.framework.commons.TimeProperties;
import io.minio.MinioClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
    @NoArgsConstructor
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
        private String downloadUrl = "http://localhost:9010/api/v1/buckets/{bucketName}/objects/download?prefix={filename}";

        /**
         * 自动删除配置
         */
        private AutoDeleteProperties autoDelete;

    }

    /**
     * 自动删除配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class AutoDeleteProperties {

        /**
         * 自动删除调度表达式
         */
        private String cron = "0 1 * * * ?";

        /**
         * 过期的桶映射 map
         */
        private Map<String, TimeProperties> expiration;

    }

}
