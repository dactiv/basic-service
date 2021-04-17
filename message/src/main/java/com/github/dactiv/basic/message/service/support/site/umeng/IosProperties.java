package com.github.dactiv.basic.message.service.support.site.umeng;

/**
 * ios 配置信息
 *
 * @author maurice
 */
public class IosProperties {

    private String appKey;

    private String secretKey;

    public IosProperties() {
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
