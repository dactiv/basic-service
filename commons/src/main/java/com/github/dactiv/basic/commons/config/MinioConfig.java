package com.github.dactiv.basic.commons.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * minio 配置信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MinioConfig {

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
}
