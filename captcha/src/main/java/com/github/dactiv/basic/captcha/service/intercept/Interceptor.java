package com.github.dactiv.basic.captcha.service.intercept;

import com.github.dactiv.basic.captcha.service.BuildToken;
import com.github.dactiv.framework.commons.RestResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 拦截器，主要是在生成验证码之前，在校验多一次验证码而使用的接口，可以使用拦截器去防止被刷的可能。
 *
 * @author maurice
 */
public interface Interceptor {

    /**
     * 生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截的验证码类型
     * @param interceptType 拦截的 token 类型
     * @return 绑定 token
     */
    BuildToken generateCaptchaIntercept(String token, String type, String interceptType);

    /**
     * 校验验证码
     *
     * @param request http servlet request
     * @return 校验结果
     */
    RestResult<Map<String, Object>> verifyCaptcha(HttpServletRequest request);
}
