package com.github.dactiv.basic.authentication.security.handler;

import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.enumerate.LoginTypeEnum;
import com.github.dactiv.basic.commons.feign.captcha.CaptchaFeignClient;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
public class CaptchaAuthenticationFailureResponse implements JsonAuthenticationFailureResponse {

    public static final String DEFAULT_TYPE_PARAM_NAME = "type";

    public static final String DEFAULT_MOBILE_CAPTCHA_TYPE = "sms";

    public static final String CAPTCHA_EXECUTE_CODE = "1001";

    private final ApplicationConfig applicationConfig;

    private final AuthenticationProperties properties;

    private final CaptchaFeignClient captchaFeignClient;

    private final RedissonClient redissonClient;

    public CaptchaAuthenticationFailureResponse(ApplicationConfig applicationConfig,
                                                AuthenticationProperties properties,
                                                CaptchaFeignClient captchaFeignClient,
                                                RedissonClient redissonClient) {
        this.applicationConfig = applicationConfig;
        this.properties = properties;
        this.captchaFeignClient = captchaFeignClient;
        this.redissonClient = redissonClient;
    }

    @Override
    public void setting(RestResult<Map<String, Object>> result, HttpServletRequest request) {

        Map<String, Object> data = result.getData();

        // 获取错误次数
        Integer number = getAllowableFailureNumber(request);

        String type = request.getHeader(properties.getTypeHeaderName());

        if (applicationConfig.getCaptchaAuthenticationTypes().contains(type)) {
            setAllowableFailureNumber(request, ++number);
        }

        if (number < applicationConfig.getAllowableFailureNumber()) {
            return;
        }

        // 获取设备唯一识别
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String loginType = request.getParameter(DEFAULT_TYPE_PARAM_NAME);

        // 如果登录类型为用户名密码登录的情况下，创建一个生成验证码 token 给客户端生成验证码，
        // 该验证码会通过 CaptchaAuthenticationFilter 进行验证码，详情查看 CaptchaAuthenticationFilter。
        // 如果登录类型为手机短信登录，创建一个生成短信发送验证码的拦截 token 给客户端，
        // 让客户端在页面生成一个验证码，该验证码为发送短信时需要验证的验证码，方式短信被刷行为。
        if (LoginTypeEnum.Mobile.toString().equals(loginType)) {

            String token = request.getParameter(applicationConfig.getSmsCaptchaParamName());

            if (StringUtils.isNotBlank(token)) {
                Map<String, Object> buildToken = captchaFeignClient.createGenerateCaptchaIntercept(
                        token,
                        applicationConfig.getMobileFailureCaptchaType(),
                        DEFAULT_MOBILE_CAPTCHA_TYPE
                );

                data.putAll(buildToken);
            }

        } else {
            Map<String, Object> buildToken = captchaFeignClient.generateToken(
                    applicationConfig.getUsernameFailureCaptchaType(),
                    identified
            );
            data.putAll(buildToken);
        }

        result.setExecuteCode(CAPTCHA_EXECUTE_CODE);
    }

    /**
     * 是否需要验证码认证
     *
     * @param request 请求信息
     *
     * @return true 是，否则 false
     */
    public boolean isCaptchaAuthentication(HttpServletRequest request) {
        Integer number = getAllowableFailureNumber(request);
        String type = request.getParameter(DEFAULT_TYPE_PARAM_NAME);
        return number > applicationConfig.getAllowableFailureNumber() && applicationConfig.getCaptchaAuthenticationTypes().contains(type);
    }

    /**
     * 删除允许认证失败次数
     *
     * @param request 请求信息
     */
    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        redissonClient.getBucket(key).deleteAsync();
    }

    /**
     * 设置允许认证失败次数
     *
     * @param request 请求信息
     * @param number  错误次数
     */
    private void setAllowableFailureNumber(HttpServletRequest request, Integer number) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        TimeProperties properties = applicationConfig.getAllowableFailureNumberExpireTime();

        redissonClient.getBucket(key).set(number, properties.getValue(), properties.getUnit());
    }

    /**
     * 获取允许认证失败次数
     *
     * @param request 请求信息
     *
     * @return 允许认证失败次数
     */
    public Integer getAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = getAllowableFailureNumberKey(identified);

        Integer value = redissonClient.<Integer>getBucket(key).get();

        if (value == null) {
            return 0;
        }

        return value;
    }

    /**
     * 获取允许认证失败次数 key 名称
     *
     * @param identified 唯一识别
     *
     * @return 允许认证失败次数 key 名称
     */
    private String getAllowableFailureNumberKey(String identified) {
        return applicationConfig.getAllowableFailureNumberKeyPrefix() + identified;
    }

    /**
     * 获取验证码服务
     *
     * @return 验证码服务
     */
    public CaptchaFeignClient getCaptchaService() {
        return captchaFeignClient;
    }
}
