package com.fuyu.basic.support.security.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * spring security 资源授权现实
 *
 * @author maurice.chen
 */
public class ResourceAuthority implements GrantedAuthority {

    private static final long serialVersionUID = -8731394878680391630L;

    public static final String DEFAULT_RESOURCE_PREFIX = "perms[";

    public static final String DEFAULT_RESOURCE_SUFFIX = "]";

    /**
     * spring security authority 的 name 值
     */
    private String authority;

    /**
     * 名称
     */
    private String name;

    /**
     * uri 拦截值
     */
    private String value;

    /**
     * spring security 资源授权现实
     *
     * @param authority spring security authority 的 name 值
     * @param name      名称
     * @param value     uri 拦截值
     */
    public ResourceAuthority(String authority, String name, String value) {
        this.authority = authority;
        this.name = name;
        this.value = value;
    }

    /**
     * spring security 资源授权现实
     */
    public ResourceAuthority() {
    }

    /**
     * 获取资源名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置资源名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取拦截值
     *
     * @return 拦截值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置拦截值
     *
     * @param value 拦截值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 设置 spring security authority 的 name 值
     *
     * @param authority spring security authority 的 name 值
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
