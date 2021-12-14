package com.github.dactiv.basic.message.domain.meta.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓 Payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayloadMeta {

    private String displayType;

    private AndroidPayloadBodyMeta body;

    private Map<String, Object> extra = new LinkedHashMap<>();

    public AndroidPayloadMeta() {
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public AndroidPayloadBodyMeta getBody() {
        return body;
    }

    public void setBody(AndroidPayloadBodyMeta body) {
        this.body = body;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
