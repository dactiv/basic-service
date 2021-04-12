package com.fuyu.basic.support.security.authentication.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 当前用户认证 token
 *
 * @author maurice.chen
 */
public class PrincipalAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3747271533448473641L;

    /**
     * 当前用户
     */
    private final Object principal;

    /**
     * 认证票据（密码）
     */
    private final Object credentials;

    /**
     * 用户类型
     */
    private final String type;

    /**
     * 当前用户认证 token
     *
     * @param principal   当前用户
     * @param credentials 认证票据（密码）
     * @param type        用户类型
     */
    public PrincipalAuthenticationToken(Object principal,
                                        Object credentials,
                                        String type) {
        this(principal, credentials, type, new ArrayList<>());
    }

    /**
     * 当前用户认证 token
     *
     * @param principal   当前用户
     * @param credentials 认证票据（密码）
     * @param type        用户类型
     * @param userDetails 用户明细实体
     * @param authorities 授权信息
     */
    public PrincipalAuthenticationToken(Object principal,
                                        Object credentials,
                                        String type,
                                        UserDetails userDetails,
                                        Collection<? extends GrantedAuthority> authorities) {

        this(principal, credentials, type, authorities);
        setAuthenticated(true);
        setDetails(userDetails);
    }

    /**
     * 当前用户认证 token
     *
     * @param principal   当前用户
     * @param credentials 认证票据（密码）
     * @param type        用户类型
     * @param authorities 授权信息
     */
    public PrincipalAuthenticationToken(Object principal,
                                        Object credentials,
                                        String type,
                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.type = type;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return getType() + ":" + principal;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }
}
