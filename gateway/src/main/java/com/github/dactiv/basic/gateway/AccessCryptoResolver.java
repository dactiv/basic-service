package com.github.dactiv.basic.gateway;

import com.github.dactiv.framework.crypto.access.AccessCrypto;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问加解密解析器
 *
 * @author maurice
 */
public interface AccessCryptoResolver {

    /**
     * 获取访问加解密集合
     *
     * @return 访问加解密集合
     */
    default List<AccessCrypto> getAccessCryptoList() {
        return new ArrayList<>();
    }

    /**
     * 是否需要请求解密
     *
     * @param accessCrypto      访问加解密
     * @param serverWebExchange Server Web Exchange
     *
     * @return true 是，否则 false
     */
    default boolean isRequestDecrypt(AccessCrypto accessCrypto, ServerWebExchange serverWebExchange) {
        return false;
    }

    /**
     * 解密请求内容
     *
     * @param serverWebExchange Server Web Exchange
     * @param accessCrypto      访问加解密
     * @param body              当前请求的内容信息
     *
     * @return 解密后的请求内容
     */
    Mono<String> decryptRequestBody(ServerWebExchange serverWebExchange, AccessCrypto accessCrypto, String body);

    /**
     * 是否需要响应加密
     *
     * @param accessCrypto      访问加解密
     * @param serverWebExchange Server Web Exchange
     *
     * @return true 是，否则 false
     */
    default boolean isResponseEncrypt(AccessCrypto accessCrypto, ServerWebExchange serverWebExchange) {
        return false;
    }

    /**
     * 加密响应内容
     *
     * @param serverWebExchange Server Web Exchange
     * @param accessCrypto      访问加解密
     * @param originalBody      当前响应的内容信息
     *
     * @return 加密后的响应的内容信息
     */
    Mono<String> encryptResponseBody(ServerWebExchange serverWebExchange, AccessCrypto accessCrypto, String originalBody);

}
