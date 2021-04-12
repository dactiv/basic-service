package com.fuyu.basic.message.service.support.site.umeng;

import java.util.ArrayList;
import java.util.List;

/**
 * 安卓配置信息实体
 *
 * @author maurice
 */
public class AndroidProperties {

    private String appKey;

    private String secretKey;

    private boolean push;

    private String activity;

    private List<String> ignoreActivityType = new ArrayList<>();

    public AndroidProperties() {

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

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public List<String> getIgnoreActivityType() {
        return ignoreActivityType;
    }

    public void setIgnoreActivityType(List<String> ignoreActivityType) {
        this.ignoreActivityType = ignoreActivityType;
    }
}
