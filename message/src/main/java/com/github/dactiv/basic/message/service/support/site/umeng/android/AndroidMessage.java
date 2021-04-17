package com.github.dactiv.basic.message.service.support.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.basic.message.service.support.site.umeng.BasicMessage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓消息实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidMessage extends BasicMessage {

    private boolean mipush;

    private String miActivity;

    private Map<String, Object> channelProperties = new LinkedHashMap<>();

    public AndroidMessage() {
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
