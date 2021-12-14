package com.github.dactiv.basic.message.domain.meta.site.ument.ios;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 友盟 ios payload aps 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadApsMeta {

    private IosPayloadApsAlertMeta alert;

    private String badge;

    private String sound;

    private Integer contentAvailable;

    private String category;

    public IosPayloadApsMeta() {
    }

    public IosPayloadApsAlertMeta getAlert() {
        return alert;
    }

    public void setAlert(IosPayloadApsAlertMeta alert) {
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
