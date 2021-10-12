package com.github.dactiv.basic.file.manager.config;


import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 *
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@ConfigurationProperties("dactiv.file-manager")
public class ApplicationConfig {

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
    private AutoDelete autoDelete;

    /**
     * 自动删除配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class AutoDelete {

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
