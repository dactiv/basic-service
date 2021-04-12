package com.fuyu.basic.message.service.support.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓 Payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayload {

    private String displayType;

    private AndroidPayloadBody body;

    private Map<String, Object> extra = new LinkedHashMap<>();

    public AndroidPayload() {
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public AndroidPayloadBody getBody() {
        return body;
    }

    public void setBody(AndroidPayloadBody body) {
        this.body = body;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
