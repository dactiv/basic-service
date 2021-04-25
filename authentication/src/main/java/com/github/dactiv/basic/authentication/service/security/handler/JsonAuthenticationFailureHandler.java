package com.github.dactiv.basic.authentication.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.authentication.service.security.CaptchaService;
import com.github.dactiv.basic.authentication.service.security.LoginType;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
@RefreshScope
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    public static final String DEFAULT_TYPE_PARAM_NAME = "type";

    public static final String DEFAULT_MOBILE_CAPTCHA_TYPE = "sms";

    /**
     * 允许认证错误次数，当达到峰值时，出现验证码
     */
    @Value("${spring.security.authentication.allow-failure-count:1}")
    private Integer allowableFailureNumber;
    /**
     * 用户名密码登录错误使用的验证码类型
     */
    @Value("${spring.security.authentication.captcha.username-failure-captcha-type:picture}")
    private String usernameFailureCaptchaType;
    /**
     * 手机号码认证错误使用的验证码类型
     */
    @Value("${spring.security.authentication.captcha.mobile-failure-captcha-type:picture}")
    private String mobileFailureCaptchaType;
    /**
     * 短信 token 参数名称，用于手机号码认证错误时，
     * 在次发送短信验证码时，需要 mobileFailureCaptchaType 类型的验证码通过才能发送短信验证码
     */
    @Value("${spring.security.authentication.captcha.sms-captcha-token-param-name:_smsCaptchaToken}")
    private String smsCaptchaParamName;

    @Value("${spring.security.authentication.allowable-failure-number-key-prefix:1800}")
    private Integer allowableFailureNumberExpireTime;

    @Value("${spring.security.authentication.allowable-failure-number-key-prefix:spring.security:authentication:failure:}")
    private String allowableFailureNumberKeyPrefix;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {

        String message = e.getMessage();

        Map<String, Object> data = new LinkedHashMap<>();

        // 获取错误次数
        Integer number = getAllowableFailureNumber(request);

        String type = request.getHeader(RequestAuthenticationFilter.SPRING_SECURITY_FORM_TYPE_HEADER_NAME);

        String executeCode = RestResult.ERROR_EXECUTE_CODE;
        // 登录或注册错输次数大于等于允许错误次数时，做一下策略
        if (number >= allowableFailureNumber) {

            // 获取设备唯一识别
            String identified = SpringMvcUtils.getDeviceIdentified(request);

            String loginType = request.getParameter(DEFAULT_TYPE_PARAM_NAME);

            // 如果登录类型为用户名密码登录的情况下，创建一个生成验证码 token 给客户端生成验证码，
            // 该验证码会通过 CaptchaAuthenticationFilter 进行验证码，详情查看 CaptchaAuthenticationFilter。
            // 如果登录类型为手机短信登录，创建一个生成短信发送验证码的拦截 token 给客户端，
            // 让客户端在页面生成一个验证码，该验证码为发送短信时需要验证的验证码，方式短信被刷行为。
            if (LoginType.Mobile.toString().equals(loginType)) {

                String token = request.getParameter(smsCaptchaParamName);

                if (StringUtils.isNotEmpty(token)) {
                    Map<String, Object> buildToken = captchaService.createGenerateCaptchaIntercept(
                            token,
                            mobileFailureCaptchaType,
                            DEFAULT_MOBILE_CAPTCHA_TYPE
                    );

                    data.putAll(buildToken);
                }

            } else {
                Map<String, Object> buildToken = captchaService.generateToken(usernameFailureCaptchaType, identified);
                data.putAll(buildToken);
            }

            executeCode = RestResult.CAPTCHA_EXECUTE_CODE;

        }

        if (!ResourceSource.Mobile.toString().equals(type)) {
            setAllowableFailureNumber(request, ++number);
        }

        RestResult<Map<String, Object>> result = new RestResult<>(
                message,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                executeCode,
                data
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    public boolean isCaptchaAuthentication(HttpServletRequest request) {
        Integer number = getAllowableFailureNumber(request);
        String type = request.getParameter(DEFAULT_TYPE_PARAM_NAME);
        return number > allowableFailureNumber && !LoginType.Mobile.toString().equals(type);
    }

    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        redisTemplate.delete(key);
    }

    private void setAllowableFailureNumber(HttpServletRequest request, Integer number) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        redisTemplate.opsForValue().set(key, number, Duration.ofSeconds(allowableFailureNumberExpireTime));
    }

    public Integer getAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0;
        }

        return Casts.cast(value);
    }

    private String getAllowableFailureNumberKey(String identified) {
        return allowableFailureNumberKeyPrefix + identified;
    }

    public Integer getAllowableFailureNumber() {
        return allowableFailureNumber;
    }

    /**
     * 获取验证码服务
     *
     * @return 验证码服务
     */
    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    public String getUsernameFailureCaptchaType() {
        return usernameFailureCaptchaType;
    }

    public String getMobileFailureCaptchaType() {
        return mobileFailureCaptchaType;
    }
}
