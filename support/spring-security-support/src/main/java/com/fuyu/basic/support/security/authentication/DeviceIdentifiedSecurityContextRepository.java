package com.fuyu.basic.support.security.authentication;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.IdEntity;
import com.fuyu.basic.support.security.entity.MobileUserDetails;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * 设备唯一识别的 spring security context 仓库实现,用于移动端通过设备唯一识别获取当前 security context 使用
 *
 * @author maurice
 *
 */
public class DeviceIdentifiedSecurityContextRepository extends HttpSessionSecurityContextRepository {

    public final static String DEFAULT_USER_ID_HEADER_NAME = "X-ACCESS-USER-ID";

    /**
     * 默认存储在 redis 的 security context key 的过期时间
     */
    private final static long DEFAULT_EXPIRES_TIME = 2592000;

    /**
     * 存储在 redis 的 security context key 名称
     */
    private String springSecurityContextKey;

    private RedisTemplate<String, Object> redisTemplate;

    private String loginProcessingUrl;

    public DeviceIdentifiedSecurityContextRepository() {
    }

    public DeviceIdentifiedSecurityContextRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public DeviceIdentifiedSecurityContextRepository(String springSecurityContextKey, RedisTemplate<String, Object> redisTemplate) {
        this.springSecurityContextKey = springSecurityContextKey;
        this.redisTemplate = redisTemplate;
    }

    public DeviceIdentifiedSecurityContextRepository(String springSecurityContextKey, RedisTemplate<String, Object> redisTemplate, String loginProcessingUrl) {
        this.springSecurityContextKey = springSecurityContextKey;
        this.redisTemplate = redisTemplate;
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        SecurityContext superSecurityContext = super.loadContext(requestResponseHolder);

        HttpServletRequest request = requestResponseHolder.getRequest();

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(token)) {

            String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

            if (StringUtils.isNotEmpty(userId)) {

                String key = getSpringSecurityContextKey(token);

                Object value = redisTemplate.opsForValue().get(key);
                SecurityContext cacheSecurityContext = Casts.cast(value);

                if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {
                    return cacheSecurityContext;
                }
            }
        }

        return superSecurityContext;
    }

    private boolean isCurrentUserSecurityContext(String userId, SecurityContext securityContext, String token) {

        if (securityContext != null && securityContext.getAuthentication() != null) {
            Object details = securityContext.getAuthentication().getDetails();

            if (SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

                SecurityUserDetails userDetails = Casts.cast(details);

                Object id = userDetails.getId();

                return (Objects.nonNull(id) && id.toString().equals(userId)) || token.equals(userId);

            } else {
                return token.equals(userId);
            }
        } else {
            return false;
        }

    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

        super.saveContext(context, request, response);

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(token)) {

            SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(
                    response,
                    SaveContextOnUpdateOrErrorResponseWrapper.class
            );

            if (Objects.nonNull(responseWrapper) && !responseWrapper.isContextSaved() && Objects.nonNull(context.getAuthentication()) && context.getAuthentication().isAuthenticated()){

                Object details = context.getAuthentication().getDetails();

                if (details != null && SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

                    String key = getSpringSecurityContextKey(token);

                    Object value = redisTemplate.opsForValue().get(key);
                    SecurityContext cacheSecurityContext = Casts.cast(value);

                    if (cacheSecurityContext != null) {

                        String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

                        if (StringUtils.isEmpty(userId) && StringUtils.equals(request.getRequestURI(), loginProcessingUrl)) {
                            redisTemplate.opsForValue().set(key, context, Duration.ofSeconds(DEFAULT_EXPIRES_TIME));
                            SecurityContextHolder.setContext(context);
                        } else if (isCurrentUserSecurityContext(userId, cacheSecurityContext, token)) {

                            String temp = key;

                            if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {

                                MobileUserDetails mobileUserDetails = Casts.cast(details);

                                if (StringUtils.isNotEmpty(mobileUserDetails.getDeviceIdentified())) {
                                    temp = getSpringSecurityContextKey(mobileUserDetails.getDeviceIdentified());
                                }
                            }

                            redisTemplate.opsForValue().set(temp, context, Duration.ofSeconds(DEFAULT_EXPIRES_TIME));
                            SecurityContextHolder.setContext(context);
                        }

                    } else {
                        redisTemplate.opsForValue().set(key, context, Duration.ofSeconds(DEFAULT_EXPIRES_TIME));
                        SecurityContextHolder.setContext(context);
                    }

                }
            }
        }

    }

    @Override
    public boolean containsContext(HttpServletRequest request) {

        boolean result = false;

        String id = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isNotEmpty(id)) {
            String token = getSpringSecurityContextKey(id);

            Object value = redisTemplate.opsForValue().get(token);

            SecurityContext securityContext = Casts.cast(value);

            String userId = request.getHeader(DEFAULT_USER_ID_HEADER_NAME);

            result = isCurrentUserSecurityContext(userId, securityContext, token) || securityContext != null;
        }

        if (!result) {
            result = super.containsContext(request);
        }

        return result;
    }

    @Override
    public void setSpringSecurityContextKey(String springSecurityContextKey) {
        super.setSpringSecurityContextKey(springSecurityContextKey);
        this.springSecurityContextKey = springSecurityContextKey;
    }

    public String getSpringSecurityContextKey(String token) {
        return springSecurityContextKey + token;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getSpringSecurityContextKey() {
        return springSecurityContextKey;
    }

    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }
}
