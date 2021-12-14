package com.github.dactiv.basic.captcha.config;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.captcha.email")
public class EmailCaptchaConfig {
    /**
     * 邮件验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(300, TimeUnit.SECONDS);
    /**
     * 提交邮件验证码的参数名称
     */
    private String captchaParamName = "_emailCaptcha";

    /**
     * 邮件验证码的随机生成数量
     */
    private Integer randomNumericCount = 4;

    /**
     * 提交邮件的参数名称
     */
    private String emailParamName = "email";
    /**
     * 提交消息类型的参数名称
     */
    private String typeParamName = "messageType";
}
