package com.github.dactiv.basic.authentication.service.security.handler;

import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.basic.authentication.service.security.AuthenticationProperties;
import com.github.dactiv.basic.authentication.service.security.LoginType;
import com.github.dactiv.basic.authentication.service.security.MobileUserDetailsService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdentifiedSecurityContextRepository;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * json 形式的登出成功具柄实现
 *
 * @author maurice.chen
 */
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final static List<String> DEFAULT_MEMBER_TYPES = Arrays.asList(
            ResourceSource.Mobile.toString(),
            ResourceSource.UserCenter.toString()
    );

    /**
     * 默认是否要跳转登录页面名称
     */
    public final static String DEFAULT_TO_LOGIN_NAME = "login";

    /**
     * 默认的 token 名称
     */
    private final static String DEFAULT_TOKEN_NAME = "token";

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private JsonAuthenticationFailureHandler failureHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceIdentifiedSecurityContextRepository deviceIdentifiedSecurityContextRepository;

    @Autowired
    private List<UserDetailsService> userDetailsServices;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpStatus httpStatus = SpringMvcUtils.getHttpStatus(response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication != null && SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {

            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails(), SecurityUserDetails.class);

            if (DEFAULT_MEMBER_TYPES.contains(userDetails.getType())) {
                String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

                deviceIdentifiedSecurityContextRepository.getSecurityContextBucket(token).deleteAsync();
            }

            clearCache(userDetails, authentication.getPrincipal().toString());
        }

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>());

        response.getWriter().write(Casts.writeValueAsString(result));
    }

    private void clearCache(SecurityUserDetails userDetails, String principal) {

        clearPrincipalCache(userDetails.getUsername());

        if (!principal.equals(userDetails.getUsername())) {
            clearPrincipalCache(principal);
        }

    }

    private void clearPrincipalCache(String principal) {
        userDetailsServices.forEach(uds -> uds.getType()
                .stream()
                .map(t -> new PrincipalAuthenticationToken(principal, null, t))
                .forEach(p -> userService.deleteRedisCache(uds, p)));

        Optional<UserDetailsService> userDetailsService = userDetailsServices.stream()
                .filter(uds -> MobileUserDetailsService.class.isAssignableFrom(uds.getClass()))
                .findFirst();

        if (userDetailsService.isPresent()) {
            MobileUserDetailsService mobileUserDetails = Casts.cast(userDetailsService.get());

            String token = mobileUserDetails.getMobileAuthenticationTokenKey(principal);
            deviceIdentifiedSecurityContextRepository.getSecurityContextBucket(token).deleteAsync();
        }
    }

    public RestResult<Map<String, Object>> createUnauthorizedResult(HttpServletRequest request) {
        Integer number = failureHandler.getAllowableFailureNumber(request);

        Integer allowableFailureNumber = authenticationProperties.getAllowableFailureNumber();

        String executeCode = ErrorCodeException.DEFAULT_EXCEPTION_CODE;

        Map<String, Object> data = new LinkedHashMap<>();

        if (number >= allowableFailureNumber) {
            // 获取设备唯一识别
            String identified = SpringMvcUtils.getDeviceIdentified(request);

            String type = request.getParameter(JsonAuthenticationFailureHandler.DEFAULT_TYPE_PARAM_NAME);
            // 如果登录类型为用户名密码登录的情况下，创建一个生成验证码 token 给客户端生成验证码，
            // 该验证码会通过 CaptchaAuthenticationFilter 进行验证码，详情查看 CaptchaAuthenticationFilter。
            // 如果登录类型为手机短信登录，创建一个生成短信发送验证码的拦截 token 给客户端，
            // 让客户端在页面生成一个验证码，该验证码为发送短信时需要验证的验证码，方式短信被刷行为。
            if (LoginType.Mobile.toString().equals(type)) {

                Map<String, Object> buildToken = failureHandler
                        .getCaptchaService()
                        .generateToken(
                                JsonAuthenticationFailureHandler.DEFAULT_MOBILE_CAPTCHA_TYPE,
                                identified
                        );

                String token = buildToken.get(DEFAULT_TOKEN_NAME).toString();

                String captchaType = authenticationProperties.getMobileFailureCaptchaType();

                Map<String, Object> interceptToken = failureHandler
                        .getCaptchaService()
                        .createGenerateCaptchaIntercept(
                                buildToken.get(DEFAULT_TOKEN_NAME).toString(),
                                captchaType,
                                JsonAuthenticationFailureHandler.DEFAULT_MOBILE_CAPTCHA_TYPE
                        );

                data.put(DEFAULT_TOKEN_NAME, token);
                data.putAll(interceptToken);

            } else {
                String captchaType = authenticationProperties.getUsernameFailureCaptchaType();

                Map<String, Object> buildToken = failureHandler
                        .getCaptchaService()
                        .generateToken(captchaType, identified);

                data.putAll(buildToken);
            }

            executeCode = JsonAuthenticationFailureHandler.CAPTCHA_EXECUTE_CODE;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean flag = AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) ||
                AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass());

        if (flag) {
            data.put(DEFAULT_TO_LOGIN_NAME, true);
            data.put(AnonymousUser.DEFAULT_ANONYMOUS_USERNAME, userService.getAnonymousUser());
        } else {
            data.put(DEFAULT_TO_LOGIN_NAME, !authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                data.put(AnonymousUser.DEFAULT_ANONYMOUS_USERNAME, userService.getAnonymousUser());
            }
        }

        return new RestResult<>(
                "未授权访问",
                HttpStatus.UNAUTHORIZED.value(),
                executeCode,
                data
        );
    }
}
