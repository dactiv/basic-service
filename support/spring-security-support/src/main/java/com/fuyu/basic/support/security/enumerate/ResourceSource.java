package com.fuyu.basic.support.security.enumerate;

import com.fuyu.basic.commons.enumerate.NameEnum;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
public enum ResourceSource implements NameEnum {

    /**
     * 前端
     */
    Front("前端"),
    /**
     * 管理后台
     */
    Console("管理后台"),
    /**
     * 用户中心
     */
    UserCenter("用户中心"),
    /**
     * 匿名用户
     */
    AnonymousUser("匿名用户"),
    /**
     * socket 用户
     */
    SocketUser("socket 用户"),
    /**
     * 系统
     */
    System("系统"),
    /**
     * 移动端
     */
    Mobile("移动端"),
    /**
     * 全部
     */
    All("全部");

    /**
     * 插件来源枚举
     *
     * @param name 中文名称
     */
    ResourceSource(String name) {
        this.name = name;
    }

    /**
     * 中文名称
     */
    private String name;

    @Override
    public String getName() {
        return name;
    }

}
