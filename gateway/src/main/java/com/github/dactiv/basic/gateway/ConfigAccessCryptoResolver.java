package com.github.dactiv.basic.gateway;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.github.dactiv.basic.commons.feign.config.ConfigFeignClient;
import com.github.dactiv.basic.gateway.config.ApplicationConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.token.SimpleExpirationToken;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * redis 访问加解密解析器实现
 *
 * @author maurice
 */
@Slf4j
@Component
public class ConfigAccessCryptoResolver extends AbstractAccessCryptoResolver implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigAccessCryptoResolver.class);

    private final ConfigFeignClient configFeignClient;

    private List<AccessCrypto> cache = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    public ConfigAccessCryptoResolver(ApplicationConfig config,
                                      ObjectProvider<RoutePredicateFactory> predicates,
                                      ObjectProvider<CipherAlgorithmService> cipherAlgorithmService,
                                      @Lazy ConfigFeignClient configFeignClient) {
        super(config, predicates.stream().collect(Collectors.toList()), cipherAlgorithmService);
        this.configFeignClient = configFeignClient;
    }

    @Override
    protected AccessToken getAccessToken(String accessToken) {

        Map<String, Object> tokenMap = configFeignClient.obtainAccessToken(accessToken);

        if (MapUtils.isEmpty(tokenMap)) {
            return null;
        }

        return Casts.convertValue(tokenMap, SimpleExpirationToken.class);
    }

    @Override
    public List<AccessCrypto> getAccessCryptoList() {
        return cache;
    }

    @NacosCronScheduled(cron = "${dactiv.gateway.crypto.access.sync-cron:0 0/3 * * * ?}")
    public void syncAccessCryptos() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            result = configFeignClient.getAllAccessCryptos();
        } catch (Exception e) {
            log.warn("获取访问加解密数据出错", e);
        }

        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        cache = result
                .stream()
                .map(m -> Casts.convertValue(m, AccessCrypto.class))
                .collect(Collectors.toList());

        LOGGER.info("同步访问加解密加载出" + cache.size() + "条记录:");

        for (AccessCrypto accessCrypto : cache) {

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
    }

    @Override
    public void run(String... args) {
        syncAccessCryptos();
    }
}
