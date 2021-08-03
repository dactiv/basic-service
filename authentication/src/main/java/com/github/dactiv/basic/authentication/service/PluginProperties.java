package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.security.plugin")
public class PluginProperties {

    /**
     * 管理员组 id
     */
    private Integer adminGroupId = 1;

    /**
     * 缓存配置
     */
    private CacheProperties cache = new CacheProperties(
            "plugin:resource:service:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

}
