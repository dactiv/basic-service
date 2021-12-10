package com.github.dactiv.basic.message.domain.model.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 友盟安卓 Payload Body 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPayloadBodyModel {

    private String ticker;

    private String title;

    private String text;

    private String icon;

    private String img;

    private String sound;

    private String builderId;

    private boolean playVibrate;

    private boolean playLights;

    private boolean playSound;

    private String afterOpen;

    private String url;

    private String activity;

    private Map<String, Object> custom;

    public AndroidPayloadBodyModel() {
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getBuilderId() {
        return builderId;
    }

    public void setBuilderId(String builderId) {
        this.builderId = builderId;
    }

    public boolean isPlayVibrate() {
        return playVibrate;
    }

    public void setPlayVibrate(boolean playVibrate) {
        this.playVibrate = playVibrate;
    }

    public boolean isPlayLights() {
        return playLights;
    }

    public void setPlayLights(boolean playLights) {
        this.playLights = playLights;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public String getAfterOpen() {
        return afterOpen;
    }

    public void setAfterOpen(String afterOpen) {
        this.afterOpen = afterOpen;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Map<String, Object> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, Object> custom) {
        this.custom = custom;
    }
}
