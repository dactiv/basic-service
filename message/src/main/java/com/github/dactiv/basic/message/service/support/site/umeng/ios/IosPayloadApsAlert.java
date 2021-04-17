package com.github.dactiv.basic.message.service.support.site.umeng.ios;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * 友盟 ios payload aps alert 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadApsAlert {

    private String title;

    private String subtitle;

    private String body;

    public IosPayloadApsAlert() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
