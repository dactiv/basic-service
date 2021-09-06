package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.basic.authentication.entity.MemberUser;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
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
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 移动端唤醒用户明细服务实现
 *
 * @author maurice
 */
@Component
public class MobileUserDetailsService extends MemberUserDetailsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MobileUserDetailsService.class);

    @Autowired
    private AuthenticationExtendProperties properties;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final DeviceResolver deviceResolver = new LiteDeviceResolver();

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        MobileUserDetails value = getMobileUserDetailsBucket(token.getPrincipal().toString()).get();

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

    /**
     * 获取移动端用户明细桶
     *
     * @param username 登陆账户
     * @return 移动端用户明细
     */
    public RBucket<MobileUserDetails> getMobileUserDetailsBucket(String username) {
        return redissonClient.getBucket(getMobileAuthenticationTokenKey(username));
    }

    /**
     * 获取存储在 redis 中的移动端唤醒 token key 名称
     *
     * @param username 登录账户
     * @return key 名称
     */
    public String getMobileAuthenticationTokenKey(String username) {
        return properties.getMobile().getCache().getName() + username;
    }

    /**
     * 通过会员用户创建 spring security 用户实现
     *
     * @param user       会员用户
     * @param identified 设备的唯一识别
     * @param request    http servlet request
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
     * @return map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createMobileAuthenticationResult(MobileUserDetails details) {

        details.setPassword(RandomStringUtils.randomAlphanumeric(properties.getRegister().getRandomPasswordCount()));

        Map<String, Object> result = Casts.convertValue(details, Map.class);

        String token = createReturnToken(details);

        result.put(properties.getMobile().getParamName(), token);

        String password = DigestUtils.md5DigestAsHex(
                (token + details.getUsername() + details.getDeviceIdentified()).getBytes()
        );

        password = appendPasswordString(password, details.getDevice());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("创建 MobileUserDetails token，当前 MobileUserDetails 密码为:"
                    + password + ",原文为:" + token + details.getUsername() + details.getDeviceIdentified());
        }

        details.setPassword(getPasswordEncoder().encode(password));

        saveMobileUserDetails(details);

        return result;
    }

    /**
     * 保存移动设备用户明细到缓存
     *
     * @param details 移动设备用户明细
     */
    private void saveMobileUserDetails(MobileUserDetails details) {
        if (Objects.nonNull(properties.getMobile().getCache())) {

            RBucket<MobileUserDetails> bucket = getMobileUserDetailsBucket(details.getUsername());

            CacheProperties cache = properties.getMobile().getCache();

            if (Objects.nonNull(cache.getExpiresTime())) {
                bucket.setAsync(details, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
            } else {
                bucket.setAsync(details);
            }

        }
    }

    /**
     * 追加密码字符串
     *
     * @param password 密码
     * @param device   设备信息
     * @return 新的字符串内容
     */
    private String appendPasswordString(String password, Device device) {
        return password + device.toString() + device.getDevicePlatform().name();
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public void onSuccessAuthentication(PrincipalAuthenticationToken result) {
        saveMobileUserDetails(Casts.cast(result.getDetails()));

        CacheProperties authorizationCache = getAuthorizationCache(result);

        if (Objects.nonNull(authorizationCache.getExpiresTime())) {
            redissonClient.getBucket(authorizationCache.getName()).expire(
                    authorizationCache.getExpiresTime().getValue(),
                    authorizationCache.getExpiresTime().getUnit()
            );
        }
    }

    @Override
    public CacheProperties getAuthenticationCache(PrincipalAuthenticationToken token) {
        return new CacheProperties(
                getMobileAuthenticationTokenKey(token.getPrincipal().toString()),
                properties.getMobile().getCache().getExpiresTime()
        );
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSource.Mobile.toString());
    }
}
