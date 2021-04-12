package com.fuyu.basic.support.security.plugin;

import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.version.VersionControl;

import java.lang.annotation.*;

/**
 * 插件信息注解
 *
 * @author maurice.chen
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * 资源名称
     *
     * @return 名称
     */
    String name();

    /**
     * 资源 icon
     *
     * @return icon
     */
    String icon() default "";

    /**
     * 唯一识别
     *
     * @return 值
     */
    String id() default "";

    /**
     * 父类识别
     *
     * @return 值
     */
    String parent() default "";

    /**
     * 类型，默认为 ResourceType.MENU
     *
     * @return 类型
     * @see ResourceType
     */
    ResourceType type() default ResourceType.Security;

    /**
     * 是否审计
     *
     * @return true 是，否则 false
     */
    boolean audit() default false;

    /**
     * 来源 ，默认为 PluginSource.ADMIN
     *
     * @return 来源
     * @see ResourceSource
     */
    ResourceSource[] source() default ResourceSource.All;

    /**
     * 顺序值，默认为 0
     *
     * @return 顺序之
     */
    int sort() default 0;

    /**
     * 备注
     *
     * @return 备注
     */
    String remark() default "";

    /**
     * 版本控制
     *
     * @return 版本控制
     */
    VersionControl[] versionControls() default {};

}
