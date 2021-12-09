package com.github.dactiv.basic.authentication.security;

import com.github.dactiv.basic.authentication.security.handler.CaptchaAuthenticationFailureResponse;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证码认证 filter 实现
 *
 * @author maurice.chen
 */
@Slf4j
public class CaptchaAuthenticationFilter extends RequestAuthenticationFilter {

    private final CaptchaAuthenticationFailureResponse handler;

    public CaptchaAuthenticationFilter(AuthenticationProperties properties,
                                       List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                       CaptchaAuthenticationFailureResponse handler) {
        super(properties, authenticationTypeTokenResolvers);
        this.handler = handler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        // 判断是否需要验证码授权
        if (handler.isCaptchaAuthentication(request)) {

            Map<String, Object> params = new LinkedHashMap<>();

            request.getParameterMap().forEach((k, v) -> {

                if (v.length > 1) {
                    params.put(k, Arrays.asList(v));
                } else {
                    params.put(k, v[0]);
                }

            });

            try {
                RestResult<Map<String, Object>> restResult = handler.getCaptchaService().verifyCaptcha(params);

                if (restResult.getStatus() != HttpStatus.OK.value()) {
                    throw new BadCredentialsException(restResult.getMessage());
                }
            } catch (Exception e) {
                log.error("调用校验验证码服务发生异常", e);
                throw new BadCredentialsException("验证码错误");
            }

        }

        return super.attemptAuthentication(request, response);
    }

}
