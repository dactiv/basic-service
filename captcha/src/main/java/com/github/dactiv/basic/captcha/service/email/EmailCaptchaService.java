package com.github.dactiv.basic.captcha.service.email;

import com.github.dactiv.basic.captcha.service.AbstractMessageCaptchaService;
import com.github.dactiv.basic.captcha.service.ReusableCaptcha;
import com.github.dactiv.basic.commons.feign.config.ConfigFeignClient;
import com.github.dactiv.basic.commons.feign.message.MessageFeignClient;
import com.github.dactiv.framework.commons.TimeProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

    private final EmailCaptchaProperties properties;

    public EmailCaptchaService(RedissonClient redissonClient,
                               @Qualifier("mvcValidator") @Autowired(required = false) Validator validator,
                               ConfigFeignClient configFeignClient,
                               MessageFeignClient messageFeignClient,
                               EmailCaptchaProperties properties) {
        super(redissonClient, validator, configFeignClient, messageFeignClient);
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> createSendMessageParam(EmailEntity entity, Map<String, Object> entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.get("value").toString(),
                entry.get("name"),
                captcha,
                properties.getCaptchaExpireTime().getValue()
        );

        // 构造参数，提交给消息服务发送信息
        param.put("title", entry.get("name"));
        param.put("content", content);
        param.put("toEmails", Collections.singletonList(entity.getEmail()));
        param.put("type", entry.get("name"));

        param.put(MessageFeignClient.DEFAULT_MESSAGE_TYPE_KEY, DEFAULT_TYPE);

        return param;
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(properties.getRandomNumericCount());
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return properties.getCaptchaExpireTime();
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return properties.getCaptchaParamName();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {

        Map<String, Object> generate = new LinkedHashMap<>();

        generate.put("emailParamName", properties.getEmailParamName());
        generate.put("typeParamName", properties.getTypeParamName());

        return generate;
    }
}
