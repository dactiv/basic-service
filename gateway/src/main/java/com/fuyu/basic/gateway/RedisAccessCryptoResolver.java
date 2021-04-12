package com.fuyu.basic.gateway;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.support.crypto.access.AccessCrypto;
import com.fuyu.basic.support.crypto.access.AccessToken;
import com.fuyu.basic.support.crypto.access.ExpirationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * redis 访问加解密解析器实现
 *
 * @author maurice
 */
@Component
@RefreshScope
public class RedisAccessCryptoResolver extends AbstractAccessCryptoResolver {

    /**
     * 存储在 redis 的访问加解密集合 key 名称
     */
    @Value("${spring.application.crypto.access.redis.access-crypto-list-key:access:crypto:all}")
    private String accessCryptoListKey;

    /**
     * 存储在 redis 的访问 token key 名称
     */
    @Value("${spring.application.crypto.access.redis.access-token-key:access:crypto:token:}")
    private String accessTokenKey;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected AccessToken getAccessToken(String accessToken) {
        String key = getAccessTokenKey(accessToken);
        AccessToken token = Casts.cast(redisTemplate.opsForValue().get(key));

        if (token != null && ExpirationToken.class.isAssignableFrom(token.getClass())) {
            ExpirationToken es = (ExpirationToken) token;
            es.setLastAccessedTime(LocalDateTime.now());
            redisTemplate.opsForValue().set(key, es, es.getMaxInactiveInterval());
        }

        return token;
    }

    /**
     * 获取响应加密访问 token key
     *
     * @param accessToken 响应加密访问 token
     * @return 响应加密访问 token key
     */
    private String getAccessTokenKey(String accessToken) {
        return accessTokenKey + accessToken;
    }

    @Override
    public List<AccessCrypto> getAccessCryptoList() {
        List<AccessCrypto> result = Casts.cast(redisTemplate.opsForValue().get(accessCryptoListKey));
        return result == null ? new LinkedList<>() : result;
    }

}
