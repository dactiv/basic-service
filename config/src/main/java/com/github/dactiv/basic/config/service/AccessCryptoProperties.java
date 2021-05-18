package com.github.dactiv.basic.config.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 访问加解密配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.application.access.crypto")
public class AccessCryptoProperties {

    /**
     * 存储在 redis 私有 token 缓存配置
     */
    private CacheProperties privateKeyCache = new CacheProperties(
            "access:crypto:token:private:",
            new TimeProperties(30, TimeUnit.SECONDS)
    );

    /**
     * 存储在 redis 访问 token 缓存配置
     */
    private CacheProperties accessTokenKeyCache = new CacheProperties(
            "access:crypto:token:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

    /**
     * 伪装访问加解密的成功信息
     */
    private String camouflageAccessCryptoName = "success access crypto";
    /**
     * 共有密钥
     */
    private String publicKey;
    /**
     * 私有密钥
     */
    private String privateKey;
}
