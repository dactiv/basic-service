package com.fuyu.basic.authentication.service.security;

import com.fuyu.basic.authentication.service.security.handler.JsonAuthenticationFailureHandler;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.support.security.authentication.RequestAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码认证 filter 实现
 *
 * @author maurice.chen
 */
public class CaptchaAuthenticationFilter extends RequestAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaAuthenticationFilter.class);

    @Autowired
    private JsonAuthenticationFailureHandler handler;

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
                LOGGER.error("调用校验验证码服务发生异常", e);
                throw new BadCredentialsException("验证码错误");
            }

        }

        return super.attemptAuthentication(request, response);
    }

}
