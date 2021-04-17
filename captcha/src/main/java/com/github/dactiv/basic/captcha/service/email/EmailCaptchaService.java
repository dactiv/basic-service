package com.github.dactiv.basic.captcha.service.email;

import com.github.dactiv.basic.captcha.service.AbstractMessageCaptchaService;
import com.github.dactiv.basic.captcha.service.ReusableCaptcha;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码服务
 *
 * @author maurice
 */
@Component
public class EmailCaptchaService extends AbstractMessageCaptchaService<EmailEntity, ReusableCaptcha> {
    /**
     * 默认的验证码服务类型名称
     */
    private static final String DEFAULT_TYPE = "email";

    /**
     * 邮件验证码的超时时间
     */
    @Value("${spring.application.captcha.token.email.expire-time:300}")
    private long captchaExpireTime;
    /**
     * 提交邮件验证码的参数名称
     */
    @Value("${spring.application.captcha.token.email.captcha-param-name:_emailCaptcha}")
    private String captchaParamName;

    /**
     * 邮件验证码的随机生成数量
     */
    @Value("${spring.application.captcha.token.email.random-numeric-count:4}")
    private Integer randomNumericCount;

    /**
     * 提交邮件的参数名称
     */
    @Value("${spring.application.captcha.token.email.email-param-name:email}")
    private String emailParamName;
    /**
     * 提交消息类型的参数名称
     */
    @Value("${spring.application.captcha.token.email.type-param-name:messageType}")
    private String typeParamName;

    @Override
    protected Map<String, Object> createSendMessageParam(EmailEntity entity, Map<String, Object> entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.get("value").toString(),
                entry.get("name"),
                captcha,
                TimeUnit.SECONDS.toMinutes(captchaExpireTime)
        );

        // 构造参数，提交给消息服务发送信息
        param.put("title", entry.get("name"));
        param.put("content", content);
        param.put("toUser", entity.getEmail());
        param.put("type", entry.get("name"));
        param.put("messageType", DEFAULT_TYPE);

        return param;
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(randomNumericCount);
    }

    @Override
    protected Duration getCaptchaExpireTime() {
        return Duration.ofSeconds(captchaExpireTime);
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return captchaParamName;
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {

        Map<String, Object> generate = new LinkedHashMap<>();

        generate.put("emailParamName", emailParamName);
        generate.put("typeParamName", typeParamName);

        return generate;
    }
}
