package com.github.dactiv.basic.message.domain.model.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 友盟安卓 Payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayloadModel {

    private String displayType;

    private AndroidPayloadBodyModel body;

    private Map<String, Object> extra = new LinkedHashMap<>();

    public AndroidPayloadModel() {
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public AndroidPayloadBodyModel getBody() {
        return body;
    }

    public void setBody(AndroidPayloadBodyModel body) {
        this.body = body;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
