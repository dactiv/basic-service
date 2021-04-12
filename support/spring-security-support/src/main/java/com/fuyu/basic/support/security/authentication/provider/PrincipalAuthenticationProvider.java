package com.fuyu.basic.support.security.authentication.provider;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.support.security.authentication.UserDetailsService;
import com.fuyu.basic.support.security.authentication.token.PrincipalAuthenticationToken;
import com.fuyu.basic.support.security.authentication.token.RequestAuthenticationToken;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 当前用户认证供应者实现
 *
 * @author maurice.chen
 */
public class PrincipalAuthenticationProvider implements AuthenticationManager, AuthenticationProvider, MessageSourceAware, InitializingBean {

    /**
     * 国际化信息
     */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /**
     * 用户明细符合集合
     */
    private List<UserDetailsService> userDetailsServices;

    /**
     * redis template
     */
    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 隐藏找不到用户异常，用登陆账户或密码错误异常
     */
    private boolean hideUserNotFoundExceptions = true;

    /**
     * 当前用户认证供应者实现
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     * @param redisTemplate       账户认证的用户明细服务集合
     */
    public PrincipalAuthenticationProvider(List<UserDetailsService> userDetailsServices,
                                           RedisTemplate<String, Object> redisTemplate) {
        this.userDetailsServices = userDetailsServices;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取 token
        RequestAuthenticationToken token = Casts.cast(authentication);

        // 通过 token 获取对应 type 实现的 UserDetailsService
        Optional<UserDetailsService> optionalUserDetailsService = getUserDetailsService(token);

        // 如果找不到，表示不支持此类型用户登陆，抛出异常
        if (!optionalUserDetailsService.isPresent()) {
            String message = messages.getMessage(
                    "PrincipalAuthenticationProvider.userDetailsServiceNotFound",
                    "找不到适用于 " + token.getType() + " 的 UserDetailsService 实现"
            );
            throw new AuthenticationServiceException(message);
        }

        // 获取实现类
        UserDetailsService userDetailsService = optionalUserDetailsService.get();

        SecurityUserDetails userDetails = null;

        // 如果启用认证缓存，从认证缓存里获取用户
        if (userDetailsService.isEnabledAuthenticationCache()) {
            String key = userDetailsService.getAuthenticationCacheName(token);
            Object value = redisTemplate.opsForValue().get(key);
            userDetails = Casts.cast(value);
        }

        //如果在缓存中找不到用户，调用 UserDetailsService 的 getAuthenticationUserDetails 方法获取当前用户
        if (Objects.isNull(userDetails)) {

            try {
                userDetails = userDetailsService.getAuthenticationUserDetails(token);
            } catch (AuthenticationException e) {
                // 如果 hideUserNotFoundExceptions true 并且是 UsernameNotFoundException 异常，抛出 用户名密码错误异常
                if (UsernameNotFoundException.class.isAssignableFrom(e.getClass()) && hideUserNotFoundExceptions) {
                    throw new BadCredentialsException(messages.getMessage(
                            "PrincipalAuthenticationProvider.badCredentials",
                            "用户名或密码错误"));
                } else {
                    throw e;
                }
            }
        }

        checkUserDetails(userDetails);

        String presentedPassword = authentication.getCredentials().toString();

        // 如果用户账户密码不正确，抛出用户名或密码错误异常
        if (!userDetailsService.matchesPassword(presentedPassword, token, userDetails)) {

            throw new BadCredentialsException(messages.getMessage(
                    "PrincipalAuthenticationProvider.badCredentials",
                    "用户名或密码错误"));
        }

        userDetails.setType(token.getType());

        Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();

        // 如果启用授权缓存，从授权缓存获取用户授权信息
        if (userDetailsService.isEnabledAuthorizationCache() && grantedAuthorities != null) {
            String key = userDetailsService.getAuthorizationCacheName(token);
            Object value = redisTemplate.opsForValue().get(key);
            grantedAuthorities = Casts.cast(value);
        }

        // 如果找不到授权信息，调用 UserDetailsService 的 getPrincipalAuthorities 方法获取当前用户授权信息
        if (grantedAuthorities == null) {

            grantedAuthorities = userDetailsService.getPrincipalAuthorities(userDetails);

            if (userDetailsService.isEnabledAuthorizationCache()) {

                Duration expiresTime = userDetailsService.getAuthorizationCacheExpiresTime();

                String key = userDetailsService.getAuthorizationCacheName(token);

                if (expiresTime == null) {
                    redisTemplate.opsForValue().set(key, grantedAuthorities);
                } else {
                    redisTemplate.opsForValue().set(key, grantedAuthorities, expiresTime);
                }
            }

        }

        // 如果启用认证缓存，存储用户信息到缓存里
        if (userDetailsService.isEnabledAuthenticationCache()) {

            Duration expiresTime = userDetailsService.getAuthenticationCacheExpiresTime();

            String key = userDetailsService.getAuthenticationCacheName(token);

            if (expiresTime == null) {
                redisTemplate.opsForValue().set(key, userDetails);
            } else {
                redisTemplate.opsForValue().set(key, userDetails, expiresTime);
            }
        }

        PrincipalAuthenticationToken result = createSuccessAuthentication(userDetails, token, grantedAuthorities);

        userDetailsService.onSuccessAuthentication(result);

        return result;
    }

    /**
     * 检查用户明细是否正确，如果错误，抛出 {@link AuthenticationException} 的异常
     *
     * @param userDetails 用户明细
     */
    protected void checkUserDetails(SecurityUserDetails userDetails) {

        // 如果获取不到用户，并且 hideUserNotFoundExceptions 等于 true 时，抛次用户名或密码错误异常，否则抛出找不到用户异常
        if (Objects.isNull(userDetails)) {
            if (hideUserNotFoundExceptions) {
                throw new BadCredentialsException(messages.getMessage(
                        "PrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else {
                throw new UsernameNotFoundException(messages.getMessage(
                        "PrincipalAuthenticationProvider.usernameNotFound",
                        "找不到用户信息"));
            }
        }

        // 如果用户被锁定，抛出用户被锁定异常
        if (!userDetails.isAccountNonLocked()) {

            throw new LockedException(messages.getMessage(
                    "PrincipalAuthenticationProvider.locked",
                    "用户已经被锁定"));
        }

        // 如果用户被禁用，抛出用户被禁用异常
        if (!userDetails.isEnabled()) {

            throw new DisabledException(messages.getMessage(
                    "PrincipalAuthenticationProvider.disabled",
                    "用户已被禁用"));
        }

        // 如果用户账户已过期，抛出用户账户已过期异常
        if (!userDetails.isAccountNonExpired()) {

            throw new AccountExpiredException(messages.getMessage(
                    "PrincipalAuthenticationProvider.expired",
                    "用户账户已过期"));
        }
    }

    /**
     * 创建认证信息
     *
     * @param userDetails        当前用户
     * @param token              当前认真 token
     * @param grantedAuthorities 当亲啊用户授权信息
     * @return spring security 认证信息
     */
    protected PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails,
                                                                       PrincipalAuthenticationToken token,
                                                                       Collection<? extends GrantedAuthority> grantedAuthorities) {

        return new PrincipalAuthenticationToken(
                token.getPrincipal(),
                token.getCredentials(),
                token.getType(),
                userDetails,
                grantedAuthorities
        );
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param token 当前用户认证 token
     * @return 用户明细服务
     */
    public Optional<UserDetailsService> getUserDetailsService(PrincipalAuthenticationToken token) {
        return userDetailsServices.stream().filter(uds -> uds.getType().contains(token.getType())).findFirst();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RequestAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(userDetailsServices, "至少要一个" + UserDetailsService.class.getName() + "接口的实现");
    }

    /**
     * 获取账户认证的用户明细服务集合
     *
     * @return 账户认证的用户明细服务集合
     */
    public List<UserDetailsService> getUserDetailsServices() {
        return userDetailsServices;
    }

    /**
     * 设置账户认证的用户明细服务集合
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     */
    public void setUserDetailsServices(List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
    }

    /**
     * 是否隐藏找不到用户异常
     *
     * @return true 是，否则 false
     */
    public boolean isHideUserNotFoundExceptions() {
        return hideUserNotFoundExceptions;
    }

    /**
     * 设置是否隐藏找不到用户异常
     *
     * @param hideUserNotFoundExceptions true 是，否则 false
     */
    public void setHideUserNotFoundExceptions(boolean hideUserNotFoundExceptions) {
        this.hideUserNotFoundExceptions = hideUserNotFoundExceptions;
    }

}
