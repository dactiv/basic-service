package com.fuyu.basic.support.security.enumerate;

import com.fuyu.basic.commons.enumerate.NameEnum;

/**
 * 资源类型枚举
 *
 * @author maurice
 */
public enum ResourceType implements NameEnum {

    /**
     * 菜单类型
     */
    Menu("菜单类型"),
    /**
     * 安全类型
     */
    Security("安全类型");

    /**
     * 资源类型枚举
     *
     * @param name 名称
     */
    ResourceType(String name) {
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

