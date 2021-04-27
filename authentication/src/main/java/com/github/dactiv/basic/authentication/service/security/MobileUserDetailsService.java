package com.github.dactiv.basic.authentication.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.authentication.dao.entity.MemberUser;
import com.github.dactiv.basic.authentication.service.security.config.AuthenticationProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.web.mobile.Device;
import com.github.dactiv.framework.spring.web.mobile.DeviceResolver;
import com.github.dactiv.framework.spring.web.mobile.LiteDeviceResolver;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 移动端唤醒用户明细服务实现
 *
 * @author maurice
 */
@Component
public class MobileUserDetailsService extends MemberUserDetailsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MobileUserDetailsService.class);

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final DeviceResolver deviceResolver = new LiteDeviceResolver();

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        MobileUserDetails value = getMobileUserDetails(token.getPrincipal().toString());

        // 如果没有信息，表示可能 token 已经超时，直接报错，让客户端可以通过 MemberUserDetailsService 登录一遍
        if (value == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        if (!value.getStatus().equals(UserStatus.Enabled.getValue())) {
            throw new DisabledException("你的账户已被"
                    + NameValueEnumUtils.getName(value.getStatus(), UserStatus.class)
                    + ", 无法认证");
        }

        return value;
    }

    @Override
    public boolean matchesPassword(String presentedPassword,
                                   RequestAuthenticationToken token,
                                   SecurityUserDetails userDetails) {

        Device device = deviceResolver.resolveDevice(token.getHttpServletRequest());

        String password = appendPasswordString(presentedPassword, device);

        return getPasswordEncoder().matches(password, userDetails.getPassword());
    }

    public MobileUserDetails getMobileUserDetails(String username) {
        String key = getMobileAuthenticationTokenKey(username);
        // 从 redis 中获取信息
        return Casts.cast(redisTemplate.opsForValue().get(key));
    }

    /**
     * 获取存储在 redis 中的移动端唤醒 token key 名称
     *
     * @param username 登录账户
     *
     * @return key 名称
     */
    public String getMobileAuthenticationTokenKey(String username) {
        return authenticationProperties.getMobile().getCacheName() + username;
    }

    /**
     * 通过会员用户创建 spring security 用户实现
     *
     * @param user       会员用户
     * @param identified 设备的唯一识别
     * @param request    http servlet request
     *
     * @return 移动端的用户明细实现
     */
    public MobileUserDetails createMobileUserDetails(MemberUser user,
                                                     String identified,
                                                     HttpServletRequest request) {

        Device device = deviceResolver.resolveDevice(request);

        return new MobileUserDetails(user.getId(), user.getUsername(), user.getPassword(), identified, device);
    }

    /**
     * 创建返回的 token
     *
     * @param details spring security 用户实现
     *
     * @return 密码
     */
    public String createReturnToken(MobileUserDetails details) {
        return DigestUtils.md5DigestAsHex(
                (details.getUsername() + details.getDeviceIdentified() + details.getPassword()).getBytes()
        );
    }

    /**
     * 创建移动端唤醒结果集
     *
     * @param details spring security 用户实现
     *
     * @return map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createMobileAuthenticationResult(MobileUserDetails details) {

        String authenticationKey = getMobileAuthenticationTokenKey(details.getUsername());

        details.setPassword(RandomStringUtils.randomAlphanumeric(authenticationProperties.getRegister().getRandomPasswordCount()));

        Map<String, Object> result = objectMapper.convertValue(details, Map.class);

        String token = createReturnToken(details);

        result.put(authenticationProperties.getMobile().getParamName(), token);

        String password = DigestUtils.md5DigestAsHex(
                (token + details.getUsername() + details.getDeviceIdentified()).getBytes()
        );

        password = appendPasswordString(password, details.getDevice());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("创建 MobileUserDetails token，当前 MobileUserDetails 密码为:"
                    + password + ",原文为:" + token + details.getUsername() + details.getDeviceIdentified());
        }

        details.setPassword(getPasswordEncoder().encode(password));

        TimeProperties time = authenticationProperties.getMobile().getExpiresTime();

        redisTemplate.opsForValue().set(authenticationKey, details, time.getValue(), time.getUnit());

        return result;
    }

    private String appendPasswordString(String password, Device device) {
        return password + device.toString() + device.getDevicePlatform().name();
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public void onSuccessAuthentication(PrincipalAuthenticationToken result) {
        String authenticationKey = getMobileAuthenticationTokenKey(result.getPrincipal().toString());
        TimeProperties time = authenticationProperties.getMobile().getExpiresTime();
        redisTemplate.opsForValue().set(authenticationKey, result.getDetails(), time.getValue(), time.getUnit());

        String authorizationKey = getAuthorizationCacheName(result);
        redisTemplate.expire(authorizationKey, time.getValue(), time.getUnit());
    }

    @Override
    public String getAuthenticationCacheName(PrincipalAuthenticationToken token) {
        return getMobileAuthenticationTokenKey(token.getPrincipal().toString());
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSource.Mobile.toString());
    }

    @Override
    public TimeProperties getAuthorizationCacheExpiresTime() {
        return authenticationProperties.getMobile().getExpiresTime();
    }

    /**
     * 不启动认证缓存，因为本身就是用 {@link #getMobileAuthenticationTokenKey(String)} 来做认证缓存
     *
     * @return false
     */
    @Override
    public boolean isEnabledAuthenticationCache() {
        return false;
    }
}
