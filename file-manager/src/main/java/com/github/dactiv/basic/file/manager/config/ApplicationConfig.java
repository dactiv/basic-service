package com.github.dactiv.basic.file.manager.config;


import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
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
    private AutoDelete autoDelete = new AutoDelete();

    /**
     * 用户头像配置
     */
    private UserAvatar userAvatar = new UserAvatar();

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

    /**
     * 用户头像配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class UserAvatar {
        /**
         * 桶名称
         */
        private String bucketName = "user.avatar";

        /**
         * 历史文件 token
         */
        private String historyFileToken = "user_avatar_history_{0}.json";

        /**
         * 当前使用的头像名称
         */
        private String CurrentUseFileToken = "current_{0}";

        /**
         * 保留的历史头像记录总数
         */
        private Integer historyCount = 10;

        /**
         * 用户来源，用于默认服务启动时创建桶使用。
         */
        private List<String> userSources = Arrays.asList("Console", "UserCenter", "SocketUser");
    }
}
