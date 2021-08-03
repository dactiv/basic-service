package com.github.dactiv.basic.authentication.service.security.handler;

import com.github.dactiv.basic.authentication.service.security.AuthenticationProperties;
import com.github.dactiv.basic.authentication.service.security.CaptchaService;
import com.github.dactiv.basic.authentication.service.security.LoginType;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    public static final String DEFAULT_TYPE_PARAM_NAME = "type";

    public static final String DEFAULT_MOBILE_CAPTCHA_TYPE = "sms";

    public static final String CAPTCHA_EXECUTE_CODE = "1001";

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {

        String message = e.getMessage();

        Map<String, Object> data = new LinkedHashMap<>();

        // 获取错误次数
        Integer number = getAllowableFailureNumber(request);

        String type = request.getHeader(RequestAuthenticationFilter.SPRING_SECURITY_FORM_TYPE_HEADER_NAME);

        String executeCode = ErrorCodeException.DEFAULT_EXCEPTION_CODE;
        // 登录或注册错输次数大于等于允许错误次数时，做一下策略
        if (number >= authenticationProperties.getAllowableFailureNumber()) {

            // 获取设备唯一识别
            String identified = SpringMvcUtils.getDeviceIdentified(request);

            String loginType = request.getParameter(DEFAULT_TYPE_PARAM_NAME);

            // 如果登录类型为用户名密码登录的情况下，创建一个生成验证码 token 给客户端生成验证码，
            // 该验证码会通过 CaptchaAuthenticationFilter 进行验证码，详情查看 CaptchaAuthenticationFilter。
            // 如果登录类型为手机短信登录，创建一个生成短信发送验证码的拦截 token 给客户端，
            // 让客户端在页面生成一个验证码，该验证码为发送短信时需要验证的验证码，方式短信被刷行为。
            if (LoginType.Mobile.toString().equals(loginType)) {

                String token = request.getParameter(authenticationProperties.getSmsCaptchaParamName());

                if (StringUtils.isNotEmpty(token)) {
                    Map<String, Object> buildToken = captchaService.createGenerateCaptchaIntercept(
                            token,
                            authenticationProperties.getMobileFailureCaptchaType(),
                            DEFAULT_MOBILE_CAPTCHA_TYPE
                    );

                    data.putAll(buildToken);
                }

            } else {
                Map<String, Object> buildToken = captchaService.generateToken(
                        authenticationProperties.getUsernameFailureCaptchaType(),
                        identified
                );
                data.putAll(buildToken);
            }

            executeCode = CAPTCHA_EXECUTE_CODE;

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

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }

    public boolean isCaptchaAuthentication(HttpServletRequest request) {
        Integer number = getAllowableFailureNumber(request);
        String type = request.getParameter(DEFAULT_TYPE_PARAM_NAME);
        return number > authenticationProperties.getAllowableFailureNumber() && !LoginType.Mobile.toString().equals(type);
    }

    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        redissonClient.getBucket(key).deleteAsync();
    }

    private void setAllowableFailureNumber(HttpServletRequest request, Integer number) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        TimeProperties properties = authenticationProperties.getAllowableFailureNumberExpireTime();

        redissonClient.getBucket(key).set(number, properties.getValue(), properties.getUnit());
    }

    public Integer getAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        Integer value = redissonClient.<Integer>getBucket(key).get();

        if (value == null) {
            return 0;
        }

        return value;
    }

    private String getAllowableFailureNumberKey(String identified) {
        return authenticationProperties.getAllowableFailureNumberKeyPrefix() + identified;
    }

    /**
     * 获取验证码服务
     *
     * @return 验证码服务
     */
    public CaptchaService getCaptchaService() {
        return captchaService;
    }
}
