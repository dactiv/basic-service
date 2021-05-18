package com.github.dactiv.basic.captcha.controller;

import com.github.dactiv.basic.captcha.service.BuildToken;
import com.github.dactiv.basic.captcha.service.DelegateCaptchaService;
import com.github.dactiv.basic.captcha.service.SimpleBuildToken;
import com.github.dactiv.basic.captcha.service.intercept.Interceptor;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 验证码控制器
 *
 * @author maurice
 */
@RestController
public class CaptchaController {

    @Autowired
    private DelegateCaptchaService delegateCaptchaService;

    @Autowired
    private Interceptor interceptor;

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     * @return 绑定 token
     */
    @PreAuthorize("hasRole('BASIC')")
    @PostMapping("createGenerateCaptchaIntercept")
    public BuildToken createGenerateCaptchaIntercept(@RequestParam String token,
                                                     @RequestParam String type,
                                                     @RequestParam String interceptType) {

        BuildToken buildToken = interceptor.generateCaptchaIntercept(token, type, interceptType);

        if (SimpleBuildToken.class.isAssignableFrom(buildToken.getClass())) {

            SimpleBuildToken simpleBuildToken = Casts.cast(buildToken);

            // 如果类型为 SimpleBuildToken 类型，id,paramName,token 不响应
            simpleBuildToken.setId(null);
            simpleBuildToken.setParamName(null);
            simpleBuildToken.setToken(null);

        }

        return buildToken;
    }

    /**
     * 生成 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 设备唯一识别
     * @return 创建绑定 token
     */
    @GetMapping("generateToken")
    public BuildToken generateToken(@RequestParam String type,
                                    @RequestParam(required = false) String deviceIdentified) {


        if (StringUtils.isEmpty(deviceIdentified)) {
            deviceIdentified = SpringMvcUtils.getDeviceIdentified();
        }

        return delegateCaptchaService.generateToken(type, deviceIdentified);
    }

    /**
     * 生成验证码
     *
     * @param request http servlet request
     * @return 验证码
     * @throws Exception 生成错误时抛出
     */
    @PostMapping("generateCaptcha")
    public Object generateCaptcha(HttpServletRequest request) throws Exception {

        RestResult<Map<String, Object>> result = interceptor.verifyCaptcha(request);

        if (result.getStatus() == HttpStatus.OK.value()) {
            return delegateCaptchaService.generateCaptcha(request);
        }

        return result;
    }

    /**
     * 校验验证码
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @PostMapping("verifyCaptcha")
    public RestResult<Map<String, Object>> verifyCaptcha(HttpServletRequest request) {
        return delegateCaptchaService.verify(request);
    }

}
