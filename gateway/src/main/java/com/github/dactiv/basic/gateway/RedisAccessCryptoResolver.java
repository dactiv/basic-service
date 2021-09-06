package com.github.dactiv.basic.gateway;

import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.ExpirationToken;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * redis 访问加解密解析器实现
 *
 * @author maurice
 */
@Component
@RefreshScope
public class RedisAccessCryptoResolver extends AbstractAccessCryptoResolver implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAccessCryptoResolver.class);

    @Autowired
    private AccessCryptoProperties accessCryptoProperties;

    @Autowired
    private RedissonClient redissonClient;

    private List<AccessCrypto> cache = new ArrayList<>();

    @Override
    protected AccessToken getAccessToken(String accessToken) {

        RBucket<AccessToken> bucket = getAccessTokenBucket(accessToken);

        AccessToken token = bucket.get();

        if (token != null && ExpirationToken.class.isAssignableFrom(token.getClass())) {

            ExpirationToken es = (ExpirationToken) token;
            es.setLastAccessedTime(LocalDateTime.now());

            if (Objects.nonNull(es.getMaxInactiveInterval())) {
                bucket.set(es, es.getMaxInactiveInterval().getValue(), es.getMaxInactiveInterval().getUnit());
            }

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
        return accessCryptoProperties.getAccessTokenKey() + accessToken;
    }

    public RBucket<AccessToken> getAccessTokenBucket(String accessToken) {
        return redissonClient.getBucket(getAccessTokenKey(accessToken));
    }

    @Override
    public List<AccessCrypto> getAccessCryptoList() {
        return cache;
    }

    @Override
    public void afterPropertiesSet() {
        syncRedisAccessCryptoList();
    }

    @NacosCronScheduled(cron = "${spring.application.crypto.access.redis.sync-cron:0 0/3 * * * ?}")
    public void syncRedisAccessCryptoList() {
        RList<AccessCrypto> list = redissonClient.getList(accessCryptoProperties.getAccessCryptoListKey());
        RFuture<List<AccessCrypto>> future = list.rangeAsync(0, list.size());

        future.onComplete((accessCryptos, throwable) -> {
            if (Objects.isNull(throwable)) {

                LOGGER.info("同步 redis 访问加解密加载出" + accessCryptos.size() + "条记录:");

                for (AccessCrypto accessCrypto : accessCryptos) {

                    List<String> predicateString = accessCrypto
                            .getPredicates()
                            .stream()
                            .map(p -> MessageFormat.format("name = {0}, value={1} ", p.getName(), p.getValue()))
                            .collect(Collectors.toList());

                    LOGGER.info(
                            "[name={},type={}}]:{} = [{}]",
                            accessCrypto.getName(),
                            accessCrypto.getType(),
                            accessCrypto.getValue(),
                            StringUtils.join(predicateString, StringArrayPropertyEditor.DEFAULT_SEPARATOR)
                    );
                }

                cache = accessCryptos;
                //cache.stream().filter(ac -> ac.getName())
            } else {
                LOGGER.warn("同步 redis 访问加解密加载出错", throwable);
            }
        });
    }

}
