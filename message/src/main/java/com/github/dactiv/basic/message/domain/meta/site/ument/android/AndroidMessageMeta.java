package com.github.dactiv.basic.message.domain.meta.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.basic.message.domain.meta.site.ument.BasicMessageMeta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓消息实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidMessageMeta extends BasicMessageMeta {

    private boolean mipush;

    private String miActivity;

    private Map<String, Object> channelProperties = new LinkedHashMap<>();

    public AndroidMessageMeta() {
    }

    public boolean isMipush() {
        return mipush;
    }

    public void setMipush(boolean mipush) {
        this.mipush = mipush;
    }

    public String getMiActivity() {
        return miActivity;
    }

    public void setMiActivity(String miActivity) {
        this.miActivity = miActivity;
    }

    public Map<String, Object> getChannelProperties() {
        return channelProperties;
    }

    public void setChannelProperties(Map<String, Object> channelProperties) {
        this.channelProperties = channelProperties;
    }
}
