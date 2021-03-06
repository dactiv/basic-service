package com.github.dactiv.basic.commons.feign.captcha;

import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 验证码服务
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_CAPTCHA_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface CaptchaFeignClient {

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     *
     * @return 绑定 token
     */
    @PostMapping("createGenerateCaptchaIntercept")
    Map<String, Object> createGenerateCaptchaIntercept(@RequestParam("token") String token,
                                                       @RequestParam("type") String type,
                                                       @RequestParam("interceptType") String interceptType);

    /**
     * 创建绑定 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 唯一识别
     *
     * @return 绑定 token
     */
    @GetMapping("generateToken")
    Map<String, Object> generateToken(@RequestParam("type") String type,
                                      @RequestParam("deviceIdentified") String deviceIdentified);

    /**
     * 校验验证码
     *
     * @param param 参数信息
     *
     * @return rest 结果集
     */
    @PostMapping("verifyCaptcha")
    RestResult<Map<String, Object>> verifyCaptcha(@RequestParam Map<String, Object> param);
}
