package com.github.dactiv.basic.config.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.AlgorithmProperties;
import com.github.dactiv.framework.crypto.RsaProperties;
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
@ConfigurationProperties("dactiv.config")
public class ApplicationConfig {

    /**
     * 存储在 redis 私有 token 缓存配置
     */
    private CacheProperties privateKeyCache = CacheProperties.of(
            "access:crypto:token:private:",
            new TimeProperties(30, TimeUnit.SECONDS)
    );

    /**
     * 存储在 redis 访问 token 缓存配置
     */
    private CacheProperties accessTokenKeyCache = CacheProperties.of(
            "access:crypto:token:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

    /**
     * 存储所有访问加解密的缓存配置
     */
    private CacheProperties accessCryptoCache = CacheProperties.of(
            "access:crypto:all:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

    /**
     * 伪装访问加解密的成功信息
     */
    private String camouflageAccessCryptoName = "success access crypto";

    /**
     * 加解密算法配置
     */
    private AlgorithmProperties algorithm;

    /**
     * rsa 配置
     */
    private RsaProperties rsa;

    /**
     * 字典配置
     */
    private Dictionary dictionary = new Dictionary();

    @Data
    @NoArgsConstructor
    public static class Dictionary {

        private String separator = ".";
    }
}
