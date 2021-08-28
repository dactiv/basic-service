package com.github.dactiv.basic.message.service.support.site.umeng;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 友盟站内信配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "message.site.umeng")
public class SiteProperties {
    /**
     * 接口调用地址
     */
    private String url;
    /**
     * 环境配置
     */
    private boolean productionMode;

    /**
     * 消息超时时间
     */
    private TimeProperties expireTime;

    /**
     * 安卓配置项
     */
    private AndroidProperties android;
    /**
     * ios 配置项
     */
    private IosProperties ios;
}
