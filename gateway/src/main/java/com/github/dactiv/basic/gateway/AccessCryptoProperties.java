package com.github.dactiv.basic.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 访问加解密配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "crypto.access")
public class AccessCryptoProperties {

    /**
     * request 中的客户端密文参数名
     */
    private String cipherTextParamName = "cipherText";

    /**
     * 访问 token header 名称
     */
    private String accessTokenHeaders = "X-ACCESS-TOKEN";

    /**
     * 参数与值的分隔符
     */
    private String paramNameValueDelimiter = "=";

    /**
     * 存储在 redis 的访问加解密集合 key 名称
     */
    private String accessCryptoListKey = "access:crypto:all";

    /**
     * 存储在 redis 的访问 token key 名称
     */
    private String accessTokenKey = "access:crypto:token:";
}
