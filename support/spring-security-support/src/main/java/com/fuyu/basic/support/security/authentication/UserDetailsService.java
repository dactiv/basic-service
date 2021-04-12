package com.fuyu.basic.support.security.authentication;

import com.fuyu.basic.support.security.authentication.token.PrincipalAuthenticationToken;
import com.fuyu.basic.support.security.authentication.token.RequestAuthenticationToken;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * 账户认证的用户明细服务
 *
 * @author maurice.chen
 */
public interface UserDetailsService {
    /**
     * 认证缓存块名称
     */
    String DEFAULT_AUTHENTICATION_KEY_NAME = "spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    String DEFAULT_AUTHORIZATION_KEY_NAME = "spring:security:authorization:";

    /**
     * 获取认证用户明细
     *
     * @param token 请求认证 token
     * @return 用户明细
     * @throws AuthenticationException 认证错误抛出的异常
     */
    SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException;

    /**
     * 获取用户授权集合
     *
     * @param userDetails 当前用户明细
     * @return 用户授权集合
     */
    Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails);

    /**
     * 获取支持的用户类型
     *
     * @return 用户类型
     */
    List<String> getType();

    /**
     * 获取密码编码器
     *
     * @return 密码编码器
     */
    PasswordEncoder getPasswordEncoder();

    /**
     * 获取认证缓存才能超时时间
     *
     * @return 超时时间
     */
    default Duration getAuthenticationCacheExpiresTime() {
        return null;
    }

    /**
     * 获取授权缓存才能超时时间
     *
     * @return 超时时间
     */
    default Duration getAuthorizationCacheExpiresTime() {
        return null;
    }

    /**
     * 是否启用认证缓存
     *
     * @return true 是，否则 false
     */
    default boolean isEnabledAuthenticationCache() {
        return true;
    }

    /**
     * 是否启用授权缓存
     *
     * @return true 是，否则 false
     */
    default boolean isEnabledAuthorizationCache() {
        return true;
    }

    /**
     * 获取授权缓存名称
     *
     * @param token 当前用户认证 token
     * @return 缓存名称
     */
    default String getAuthorizationCacheName(PrincipalAuthenticationToken token) {
        return DEFAULT_AUTHORIZATION_KEY_NAME + token.getType() + ":" + token.getPrincipal();
    }

    /**
     * 获取授权缓存名称
     *
     * @param token 当前用户认证 token
     * @return 缓存名称
     */
    default String getAuthenticationCacheName(PrincipalAuthenticationToken token) {
        return DEFAULT_AUTHENTICATION_KEY_NAME + token.getType() + ":" + token.getPrincipal();
    }

    /**
     * 当认证成功的后置处理方法
     *
     * @param result 认证结果
     */
    default void onSuccessAuthentication(PrincipalAuthenticationToken result) {

    }

    /**
     * 匹配密码是否正确
     *
     * @param presentedPassword 提交过来的密码
     * @param token             请求认证 token
     * @param userDetails       spring security 用户实现
     * @return true 是，否则 false
     */
    default boolean matchesPassword(String presentedPassword,
                                    RequestAuthenticationToken token,
                                    SecurityUserDetails userDetails) {
        return getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());
    }
}
