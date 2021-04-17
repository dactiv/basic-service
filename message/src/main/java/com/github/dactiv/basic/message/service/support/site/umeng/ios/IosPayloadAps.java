package com.github.dactiv.basic.message.service.support.site.umeng.ios;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 友盟 ios payload aps 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadAps {

    private IosPayloadApsAlert alert;

    private String badge;

    private String sound;

    private Integer contentAvailable;

    private String category;

    public IosPayloadAps() {
    }

    public IosPayloadApsAlert getAlert() {
        return alert;
    }

    public void setAlert(IosPayloadApsAlert alert) {
        this.alert = alert;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Integer getContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(Integer contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
