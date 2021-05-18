package com.github.dactiv.basic.captcha.service.sms;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.application.captcha.sms")
public class SmsCaptchaProperties {
    /**
     * 短信验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(300, TimeUnit.SECONDS);
    /**
     * 提交短信验证码的参数名称
     */
    private String captchaParamName = "_smsCaptcha";

    /**
     * 短信验证码的随机生成数量
     */
    private Integer randomNumericCount = 6;

    /**
     * 提交手机号码的参数名称
     */
    private String phoneNumberParamName = "phoneNumber";
    /**
     * 提交消息类型的参数名称
     */
    private String typeParamName = "messageType";
}
