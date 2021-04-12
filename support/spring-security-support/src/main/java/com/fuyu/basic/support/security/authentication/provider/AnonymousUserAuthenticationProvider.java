package com.fuyu.basic.support.security.authentication.provider;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.support.security.authentication.UserDetailsService;
import com.fuyu.basic.support.security.entity.AnonymousUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;


/**
 * 匿名用户认证供应者
 *
 * @author maurice
 */
public class AnonymousUserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DEFAULT_KEY_PREFIX = UserDetailsService.DEFAULT_AUTHENTICATION_KEY_NAME;

    public AnonymousUserAuthenticationProvider(RedisTemplate<String, Object> redisTemplate) {
        setForcePrincipalAsString(true);
        this.redisTemplate = redisTemplate;
    }

    public void createUser(AnonymousUser user) {
        redisTemplate.opsForValue().set(DEFAULT_KEY_PREFIX + user.getUsername(), user);
    }

    public void deleteUser(String username) {
        redisTemplate.delete(DEFAULT_KEY_PREFIX + username);
    }

    public void updateUser(AnonymousUser user) {

        Assert.isTrue(userExists(user.getUsername()), "不存在[" + user.getUsername() + "]");

        redisTemplate.opsForValue().set(DEFAULT_KEY_PREFIX + user.getUsername(), user);
    }

    public boolean userExists(String username) {
        return redisTemplate.opsForValue().get(DEFAULT_KEY_PREFIX + username) != null;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名密码错误"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "用户名密码错误"));
        }

    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal,
                authentication.getCredentials(),
                user.getAuthorities()
        );

        result.setDetails(user);

        return result;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        try {


            AnonymousUser loadedUser = null;

            Object value = redisTemplate.opsForValue().get(DEFAULT_KEY_PREFIX + username);

            if (value != null) {
                loadedUser = Casts.cast(value);
            }

            if (loadedUser == null) {
                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "用户名密码错误"));
            }

            return new AnonymousUser(
                    loadedUser.getUsername(),
                    loadedUser.getPassword(),
                    loadedUser.getTempPassword(),
                    loadedUser.getAuthorities()
            );
        } catch (UsernameNotFoundException | InternalAuthenticationServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

}
