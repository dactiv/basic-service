package com.fuyu.basic.support.security.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Map;

/**
 * spring security 角色授权现实
 *
 * @author maurice.chen
 */
public class RoleAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 405960799851529326L;

    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    public static final String DEFAULT_NAME_FIELD_NAME = "name";

    public static final String DEFAULT_AUTHORITY_FIELD_NAME = "authority";

    /**
     * 名称
     */
    private String name;

    /**
     * spring security role 的 authority name 值
     */
    private String authority;

    /**
     * spring security 角色授权现实
     *
     * @param name      名称
     * @param authority spring security role 的 authority name 值
     */
    public RoleAuthority(String name, String authority) {
        this.name = name;
        this.authority = authority;
    }

    public RoleAuthority(Map<String, Object> map) {
        this(map.get(DEFAULT_NAME_FIELD_NAME).toString(), map.get(DEFAULT_AUTHORITY_FIELD_NAME).toString());
    }

    /**
     * spring security 角色授权现实
     */
    public RoleAuthority() {
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 spring security role 的 authority name 值
     *
     * @param authority spring security role 的 authority name 值
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
