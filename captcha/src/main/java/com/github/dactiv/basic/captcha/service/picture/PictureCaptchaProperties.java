package com.github.dactiv.basic.captcha.service.picture;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 图片验证码配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.application.captcha.picture")
public class PictureCaptchaProperties {

    /**
     * 提交验证码的参数名称
     */
    private String captchaParamName = "_pictureCaptcha";

    /**
     * 验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(900, TimeUnit.SECONDS);
}
