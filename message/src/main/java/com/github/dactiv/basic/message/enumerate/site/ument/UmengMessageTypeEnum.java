package com.github.dactiv.basic.message.enumerate.site.ument;

import com.github.dactiv.framework.commons.enumerate.NameEnum;

/**
 * 消息类型
 *
 * @author maurice
 */
public enum UmengMessageTypeEnum implements NameEnum {
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
    UmengMessageTypeEnum(String name) {
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
