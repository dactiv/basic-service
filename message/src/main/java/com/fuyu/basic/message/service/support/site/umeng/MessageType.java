package com.fuyu.basic.message.service.support.site.umeng;

import com.fuyu.basic.commons.enumerate.NameEnum;

/**
 * 消息类型
 *
 * @author maurice
 */
public enum MessageType implements NameEnum {
    /**
     * 自定义
     */
    Customize("customizedcast"),
    /**
     * 广播
     */
    Broad("broadcast"),
    /**
     * 组播
     */
    Group("groupcast");

    /**
     * 消息类型
     *
     * @param name 名称
     */
    MessageType(String name) {
        this.name = name;
    }

    /**
     * 名称
     */
    private String name;

    @Override
    public String getName() {
        return name;
    }
}
