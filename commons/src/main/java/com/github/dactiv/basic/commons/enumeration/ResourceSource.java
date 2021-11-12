package com.github.dactiv.basic.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameEnum;

import java.util.Arrays;
import java.util.List;

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
    private final String name;

    @Override
    public String getName() {
        return name;
    }

    /**
     * 前端应用来源值
     */
    public static final String FRONT_SOURCE_VALUE = "Front";

    /**
     * 管理后台应用来源值
     */
    public static final String CONSOLE_SOURCE_VALUE = "Console";

    public static final String SOCKET_USER_SOURCE_VALUE = "SocketUser";

    /**
     * 匿名用户应用来源值
     */
    public static final String ANONYMOUS_USER_SOURCE_VALUE = "AnonymousUser";

    /**
     * 用户中心应用来源值
     */
    public static final String USER_CENTER_SOURCE_VALUE = "UserCenter";

    /**
     * 系统应用来源值
     */
    public static final String SYSTEM_SOURCE_VALUE = "System";

    /**
     * 移动端应用来源值
     */
    public static final String MOBILE_SOURCE_VALUE = "Mobile";

    /**
     * 全部应用来源值
     */
    public static final String ALL_SOURCE_VALUE = "All";

    /**
     * 所有自动配置的可用来源值集合
     */
    public static final List<String> DEFAULT_ALL_SOURCE_VALUES = Arrays.asList(
            ResourceSource.All.toString(),
            ResourceSource.Console.toString(),
            ResourceSource.Front.toString(),
            ResourceSource.Mobile.toString(),
            ResourceSource.UserCenter.toString(),
            ResourceSource.System.toString()
    );

    /**
     * 所有可忽略的来源值集合
     */
    public static final List<String> DEFAULT_IGNORE_SOURCE_VALUES = Arrays.asList(
            ResourceSource.All.toString(),
            ResourceSource.System.toString()
    );

}
