package com.github.dactiv.basic.captcha.service.email;

import com.github.dactiv.basic.captcha.service.AbstractMessageCaptchaService;
import com.github.dactiv.basic.captcha.service.ReusableCaptcha;
import com.github.dactiv.framework.commons.TimeProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
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

    @Autowired
    private EmailCaptchaProperties properties;

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
        param.put("toUser", entity.getEmail());
        param.put("type", entry.get("name"));
        param.put("messageType", DEFAULT_TYPE);

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
