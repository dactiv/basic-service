package com.github.dactiv.basic.gateway.config;

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
@ConfigurationProperties(prefix = "dactiv.gateway.crypto.access")
public class ApplicationConfig {

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
     * 当错误获取不到响应的 ReasonPhrase 时，抛出异常的默认信息
     */
    private String defaultReasonPhrase = "服务器异常，请稍后再试。";
}
