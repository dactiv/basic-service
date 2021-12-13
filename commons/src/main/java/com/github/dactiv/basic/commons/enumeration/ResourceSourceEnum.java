package com.github.dactiv.basic.commons.enumeration;

import com.github.dactiv.framework.commons.enumerate.NameEnum;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
public enum ResourceSourceEnum implements NameEnum {

    /**
     * 前端
     */
    FRONT("前端"),
    /**
     * 管理后台
     */
    CONSOLE("管理后台"),
    /**
     * 用户中心
     */
    USER_CENTER("用户中心"),
    /**
     * 匿名用户
     */
    ANONYMOUS_USER("匿名用户"),
    /**
     * socket 用户
     */
    SOCKET_USER("socket 用户"),
    /**
     * 系统
     */
    SYSTEM("系统"),
    /**
     * 移动端
     */
    MOBILE("移动端"),
    /**
     * 全部
     */
    ALL("全部");

    /**
     * 插件来源枚举
     *
     * @param name 中文名称
     */
    ResourceSourceEnum(String name) {
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
    public static final String FRONT_SOURCE_VALUE = "FRONT";

    /**
     * 管理后台应用来源值
     */
    public static final String CONSOLE_SOURCE_VALUE = "CONSOLE";

    /**
     * socket 用户来源值
     */
    public static final String SOCKET_USER_SOURCE_VALUE = "SOCKET_USER";

    /**
     * 匿名用户应用来源值
     */
    public static final String ANONYMOUS_USER_SOURCE_VALUE = "ANONYMOUS_USER";

    /**
     * 用户中心应用来源值
     */
    public static final String USER_CENTER_SOURCE_VALUE = "USER_CENTER";

    /**
     * 系统应用来源值
     */
    public static final String SYSTEM_SOURCE_VALUE = "SYSTEM";

    /**
     * 移动端应用来源值
     */
    public static final String MOBILE_SOURCE_VALUE = "MOBILE";

    /**
     * 全部应用来源值
     */
    public static final String ALL_SOURCE_VALUE = "ALL";

    /**
     * 获取桶名称
     *
     * @param value 值
     *
     * @return 符合 minio 桶格式的名称
     */
    public static String getMinioBucket(String value) {
        return RegExUtils.replaceAll(value, "_", "-");
    }

}
