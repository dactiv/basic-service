package com.github.dactiv.basic.authentication.security.handler;

import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.enumerate.LoginTypeEnum;
import com.github.dactiv.basic.authentication.security.MobileUserDetailsService;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * json ?????????????????????????????????
 *
 * @author maurice.chen
 */
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final static List<String> DEFAULT_MEMBER_TYPES = Arrays.asList(
            ResourceSourceEnum.MOBILE.toString(),
            ResourceSourceEnum.USER_CENTER.toString()
    );

    /**
     * ???????????????????????????
     */
    public final static String DEFAULT_IS_AUTHENTICATION_NAME = "authentication";

    /**
     * ???????????????????????????
     */
    private final static String DEFAULT_SERVICES_NAME = "services";

    /**
     * ????????? token ??????
     */
    private final static String DEFAULT_TOKEN_NAME = "token";

    private final ApplicationConfig applicationConfig;

    private final CaptchaAuthenticationFailureResponse failureHandler;

    private final AuthorizationService authorizationService;

    private final DeviceIdContextRepository deviceIdContextRepository;

    private final List<UserDetailsService> userDetailsServices;

    private final CookieRememberService cookieRememberService;

    private final DiscoveryClient discoveryClient;

    public JsonLogoutSuccessHandler(ApplicationConfig applicationConfig,
                                    CaptchaAuthenticationFailureResponse failureHandler,
                                    AuthorizationService authorizationService,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    List<UserDetailsService> userDetailsServices,
                                    CookieRememberService cookieRememberService,
                                    DiscoveryClient discoveryClient) {
        this.applicationConfig = applicationConfig;
        this.failureHandler = failureHandler;
        this.authorizationService = authorizationService;
        this.deviceIdContextRepository = deviceIdContextRepository;
        this.userDetailsServices = userDetailsServices;
        this.cookieRememberService = cookieRememberService;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpStatus httpStatus = SpringMvcUtils.getHttpStatus(response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication != null && SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {

            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails(), SecurityUserDetails.class);

            if (DEFAULT_MEMBER_TYPES.contains(userDetails.getType())) {
                String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

                deviceIdContextRepository.getSecurityContextBucket(token).deleteAsync();
            }

            clearAllCache(userDetails, authentication.getPrincipal().toString());
        }

        cookieRememberService.loginFail(request, response);

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>());

        response.getWriter().write(Casts.writeValueAsString(result));
    }

    /**
     * ??????????????????
     *
     * @param userDetails ????????????
     * @param principal   ??????????????????
     */
    private void clearAllCache(SecurityUserDetails userDetails, String principal) {

        // ?????? username ????????????????????????????????????
        clearPrincipalCache(userDetails.getUsername());

        // ??????????????????????????????????????? principal ???????????????????????????????????? username
        if (!principal.equals(userDetails.getUsername())) {
            clearPrincipalCache(principal);
        }

    }

    /**
     * ????????????????????????????????????
     *
     * @param principal ????????????
     */
    private void clearPrincipalCache(String principal) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, null);

        userDetailsServices.forEach(uds -> uds.getType()
                .stream()
                .map(t -> new PrincipalAuthenticationToken(token, t))
                .forEach(p -> authorizationService.deleteSystemUseAuthenticationCache(uds, p)));

        Optional<UserDetailsService> userDetailsService = userDetailsServices.stream()
                .filter(uds -> MobileUserDetailsService.class.isAssignableFrom(uds.getClass()))
                .findFirst();

        if (userDetailsService.isPresent()) {
            MobileUserDetailsService mobileUserDetails = Casts.cast(userDetailsService.get());

            String key = mobileUserDetails.getMobileAuthenticationTokenKey(principal);
            deviceIdContextRepository.getSecurityContextBucket(key).deleteAsync();
        }
    }

    /**
     * ??????????????? reset ????????????????????????????????????????????????????????????????????????
     *
     * @param request ????????????
     *
     * @return rest ?????????
     */
    public RestResult<Map<String, Object>> createUnauthorizedResult(HttpServletRequest request) {

        RestResult<Map<String, Object>> result = createRestResult(request);
        postCaptchaData(result, request);
        result.getData().put(DEFAULT_SERVICES_NAME, discoveryClient.getServices());

        return result;
    }

    /**
     * ?????? reset ?????????
     *
     * @return reset ?????????
     */
    private RestResult<Map<String, Object>> createRestResult(HttpServletRequest request) {

        String executeCode = String.valueOf(HttpStatus.OK.value());

        String message = HttpStatus.OK.getReasonPhrase();

        int status = HttpStatus.OK.value();

        Map<String, Object> data = new LinkedHashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) ||
                AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass())) {
            data.put(DEFAULT_IS_AUTHENTICATION_NAME, false);
            message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
            data.put(AnonymousUser.DEFAULT_ANONYMOUS_USERNAME, authorizationService.getAnonymousUser());
        } else {
            data.put(DEFAULT_IS_AUTHENTICATION_NAME, authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
                data.put(AnonymousUser.DEFAULT_ANONYMOUS_USERNAME, authorizationService.getAnonymousUser());
            }

            if (RememberMeAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
                data.put(RememberMeProperties.DEFAULT_PARAM_NAME, true);
                data.put(RememberMeProperties.DEFAULT_USER_DETAILS_NAME, authentication.getDetails());
            } else {
                data.put(RememberMeProperties.DEFAULT_PARAM_NAME, false);
            }
        }

        String identified = StringUtils.defaultString(
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME),
                UUID.randomUUID().toString()
        );

        data.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, identified);

        return new RestResult<>(
                message,
                status,
                executeCode,
                data
        );
    }

    /**
     * ?????????????????????
     *
     * @param result  reset ?????????
     * @param request http ????????????
     */
    private void postCaptchaData(RestResult<Map<String, Object>> result, HttpServletRequest request) {
        Integer number = failureHandler.getAllowableFailureNumber(request);

        Integer allowableFailureNumber = applicationConfig.getAllowableFailureNumber();

        if (number < allowableFailureNumber) {
            return;
        }

        // ????????????????????????
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String type = request.getParameter(CaptchaAuthenticationFailureResponse.DEFAULT_TYPE_PARAM_NAME);
        // ???????????????????????????????????????????????????????????????????????????????????? token ??????????????????????????????
        // ????????????????????? CaptchaAuthenticationFilter ?????????????????????????????? CaptchaAuthenticationFilter???
        // ?????????????????????????????????????????????????????????????????????????????????????????? token ???????????????
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (LoginTypeEnum.Mobile.toString().equals(type)) {

            Map<String, Object> buildToken = failureHandler
                    .getCaptchaService()
                    .generateToken(
                            CaptchaAuthenticationFailureResponse.DEFAULT_MOBILE_CAPTCHA_TYPE,
                            identified
                    );

            String token = buildToken.get(DEFAULT_TOKEN_NAME).toString();

            String captchaType = applicationConfig.getMobileFailureCaptchaType();

            Map<String, Object> interceptToken = failureHandler
                    .getCaptchaService()
                    .createGenerateCaptchaIntercept(
                            buildToken.get(DEFAULT_TOKEN_NAME).toString(),
                            captchaType,
                            CaptchaAuthenticationFailureResponse.DEFAULT_MOBILE_CAPTCHA_TYPE
                    );

            result.getData().put(DEFAULT_TOKEN_NAME, token);
            result.getData().putAll(interceptToken);

        } else {
            String captchaType = applicationConfig.getUsernameFailureCaptchaType();

            Map<String, Object> buildToken = failureHandler
                    .getCaptchaService()
                    .generateToken(captchaType, identified);

            result.getData().putAll(buildToken);
        }
        result.setExecuteCode(CaptchaAuthenticationFailureResponse.CAPTCHA_EXECUTE_CODE);
    }
}
