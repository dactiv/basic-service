package com.fuyu.basic.captcha.service.sms;

import com.fuyu.basic.captcha.service.AbstractMessageCaptchaService;
import com.fuyu.basic.captcha.service.BuildToken;
import com.fuyu.basic.captcha.service.GenerateCaptchaResult;
import com.fuyu.basic.captcha.service.ReusableCaptcha;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务
 *
 * @author maurice
 */
@Component
public class SmsCaptchaService extends AbstractMessageCaptchaService<SmsEntity, ReusableCaptcha> {

    /**
     * 默认的验证码服务类型名称
     */
    private static final String DEFAULT_TYPE = "sms";

    /**
     * 短信验证码的超时时间
     */
    @Value("${spring.application.captcha.token.sms.expire-time:300}")
    private long captchaExpireTime;
    /**
     * 提交短信验证码的参数名称
     */
    @Value("${spring.application.captcha.token.sms.captcha-param-name:_smsCaptcha}")
    private String captchaParamName;

    /**
     * 短信验证码的随机生成数量
     */
    @Value("${spring.application.captcha.token.sms.random-numeric-count:4}")
    private Integer randomNumericCount;

    /**
     * 提交手机号码的参数名称
     */
    @Value("${spring.application.captcha.token.sms.phone-number-param-name:phoneNumber}")
    private String phoneNumberParamName;
    /**
     * 提交消息类型的参数名称
     */
    @Value("${spring.application.captcha.token.sms.type-param-name:messageType}")
    private String typeParamName;

    @Override
    protected Map<String, Object> createSendMessageParam(SmsEntity entity, Map<String, Object> entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.get("value").toString(),
                entry.get("name"),
                captcha,
                TimeUnit.SECONDS.toMinutes(captchaExpireTime)
        );

        // 构造参数，提交给消息服务发送信息
        param.put("content", content);
        param.put("phoneNumber", entity.getPhoneNumber());
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
    public String getUsernameParamName() {
        return phoneNumberParamName;
    }

    @Override
    protected Map<String, Object> createPostArgs() {
        Map<String, Object> post = super.createPostArgs();
        post.put(phoneNumberParamName, getUsernameParamName());
        return post;
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {

        Map<String, Object> generate = new LinkedHashMap<>();

        generate.put("phoneNumberParamName", getUsernameParamName());
        generate.put("typeParamName", typeParamName);

        return generate;
    }
}
