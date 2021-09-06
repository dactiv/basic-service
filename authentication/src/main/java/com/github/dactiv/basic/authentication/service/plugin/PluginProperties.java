package com.github.dactiv.basic.authentication.service.plugin;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 插件配置类
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("authentication.plugin")
public class PluginProperties {

    /**
     * 管理员组 id
     */
    private Integer adminGroupId = 1;

    /**
     * 超时时间
     */
    private TimeProperties expirationTime = new TimeProperties(1, TimeUnit.HOURS);

    /**
     * 缓存配置
     */
    @Deprecated
    private CacheProperties cache = new CacheProperties(
            "plugin:resource:service:"
    );

}
