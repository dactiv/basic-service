package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.security.plugin")
public class PluginProperties {

    /**
     * 管理员组 id
     */
    private Integer adminGroupId;

    /**
     * 缓存配置
     */
    private CacheProperties cache;

}
