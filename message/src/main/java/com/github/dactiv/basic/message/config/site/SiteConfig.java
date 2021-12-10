package com.github.dactiv.basic.message.config.site;

import com.github.dactiv.basic.message.config.site.umeng.SiteUmengConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 站内信配置
 */
@Data
@Component
@ConfigurationProperties("dactiv.message.site")
public class SiteConfig {

    /**
     * 友盟站内信配置
     */
    private SiteUmengConfig umeng;

    /**
     * 渠道商
     */
    private String channel;
}
