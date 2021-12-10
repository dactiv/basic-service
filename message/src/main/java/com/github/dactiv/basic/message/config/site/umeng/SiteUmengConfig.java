package com.github.dactiv.basic.message.config.site.umeng;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 友盟站内信配置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class SiteUmengConfig {
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
    private Android android;
    /**
     * ios 配置项
     */
    private Ios ios;
}
