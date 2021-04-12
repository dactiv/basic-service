package com.fuyu.basic.support.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fuyu.basic.commons.IdEntity;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.NameEnumUtils;
import com.fuyu.basic.commons.enumerate.NameValueEnumUtils;
import com.fuyu.basic.commons.enumerate.support.DisabledOrEnabled;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security 用户实现
 *
 * @author maurice.chen
 */
public class SecurityUserDetails extends IntegerIdEntity implements UserDetails {

    private static final long serialVersionUID = 1369484231035811533L;

    public static final String DEFAULT_IS_AUTHENTICATED_METHOD_NAME = "isAuthenticated";

    public static final String DEFAULT_HAS_ANY_ROLE_METHOD_NAME = "hasAnyRole";

    public static final String DEFAULT_HAS_ROLE_METHOD_NAME = "hasRole";

    public static final String DEFAULT_AUTHORITIES_FIELD_NAME = "authorities";

    public static final String DEFAULT_ROLE_AUTHORITIES_FIELD_NAME = "roleAuthorities";

    public static final String DEFAULT_RESOURCE_AUTHORITIES_FIELD_NAME = "resourceAuthorities";

    public static final List<String> DEFAULT_SUPPORT_SECURITY_METHOD_NAME = Arrays.asList(
            "hasAuthority",
            "hasAnyAuthority",
            DEFAULT_HAS_ROLE_METHOD_NAME,
            DEFAULT_HAS_ANY_ROLE_METHOD_NAME,
            DEFAULT_IS_AUTHENTICATED_METHOD_NAME
    );

    public static final List<String> DEFAULT_ROLE_PREFIX_METHOD_NAME = Arrays.asList(
            DEFAULT_HAS_ANY_ROLE_METHOD_NAME,
            DEFAULT_HAS_ROLE_METHOD_NAME
    );

    @JsonIgnore
    private String password;
    private String username;
    @JsonIgnore
    private List<ResourceAuthority> resourceAuthorities = new ArrayList<>();
    private List<RoleAuthority> roleAuthorities = new ArrayList<>();
    @JsonIgnore
    private boolean accountNonExpired = true;
    @JsonIgnore
    private boolean accountNonLocked = true;
    @JsonIgnore
    private boolean credentialsNonExpired = true;
    private Integer status;
    private String type;

    public SecurityUserDetails() {
    }

    public SecurityUserDetails(Integer id, String username, String password) {
        this(id, username, password, UserStatus.Enabled);
    }

    public SecurityUserDetails(Integer id, String username, String password, UserStatus userStatus) {
        this(id, username, password, userStatus, true, true, true);
    }

    public SecurityUserDetails(Integer id, String username, String password, UserStatus status,
                               boolean accountNonExpired, boolean credentialsNonExpired,
                               boolean accountNonLocked) {

        if (StringUtils.isEmpty(username) || password == null) {
            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
        }

        this.setId(id);
        this.username = username;
        this.password = password;
        this.status = status.getValue();
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> result = new ArrayList<>();

        result.addAll(resourceAuthorities.stream()
                .filter(x -> StringUtils.isNotEmpty(x.getAuthority()))
                .filter(x -> !DEFAULT_IS_AUTHENTICATED_METHOD_NAME.equals(x.getAuthority()))
                .map(x -> new SimpleGrantedAuthority(x.getAuthority()))
                .distinct()
                .collect(Collectors.toList()));

        result.addAll(roleAuthorities.stream()
                .map(x -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + x.getAuthority()))
                .distinct()
                .collect(Collectors.toList()));

        return result;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置状态:0.禁用,1启用
     *
     * @param status 状态:0.禁用,1启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取状态:0.禁用,1启用
     *
     * @return 状态:0.禁用,1启用
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(this.status, UserStatus.class);
    }

    /**
     * 设置登录帐号
     *
     * @param username 登录帐号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取登录帐号
     *
     * @return 登录帐号
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return DisabledOrEnabled.Enabled.getValue().equals(status);
    }

    public List<ResourceAuthority> getResourceAuthorities() {
        return resourceAuthorities;
    }

    public void setResourceAuthorities(List<ResourceAuthority> resourceAuthorities) {
        this.resourceAuthorities = resourceAuthorities;
    }

    public List<RoleAuthority> getRoleAuthorities() {
        return roleAuthorities;
    }

    public void setRoleAuthorities(List<RoleAuthority> roleAuthorities) {
        this.roleAuthorities = roleAuthorities;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取用户类型名称
     *
     * @return 用户类型名称
     */
    public String getTypeName() {
        String name = NameEnumUtils.getName(getType(), ResourceSource.class, true);
        return StringUtils.defaultString(name, this.type);
    }
}
