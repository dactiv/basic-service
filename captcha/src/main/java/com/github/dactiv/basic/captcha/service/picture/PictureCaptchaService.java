package com.github.dactiv.basic.captcha.service.picture;

import com.github.dactiv.basic.captcha.service.AbstractRedisCaptchaService;
import com.github.dactiv.basic.captcha.service.BuildToken;
import com.github.dactiv.basic.captcha.service.ExpiredCaptcha;
import com.github.dactiv.basic.captcha.service.GenerateCaptchaResult;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片验证码服务实现
 *
 * @author maurice
 */
@Component
public class PictureCaptchaService extends AbstractRedisCaptchaService<PictureEntity, ExpiredCaptcha> {

    /**
     * 默认的验证码服务类型名称
     */
    private static final String DEFAULT_TYPE = "picture";

    private static final String DEFAULT_BASE_64_KEY = "base64";

    @Autowired
    private List<PictureCaptchaGenerator> pictureCaptchaGeneratorList;

    /**
     * 提交验证码的参数名称
     */
    @Value("${spring.application.captcha.token.img.captcha-param-name:_pictureCaptcha}")
    private String captchaParamName;

    /**
     * 验证码的超时时间
     */
    @Value("${spring.application.captcha.token.email.expire-time:900}")
    private long captchaExpireTime;

    @Override
    protected Duration getCaptchaExpireDuration() {
        return Duration.ofSeconds(captchaExpireTime);
    }

    @Override
    protected GenerateCaptchaResult generateCaptcha(BuildToken buildToken, PictureEntity entity) throws Exception {
        // 随机抽取验证码生成器集合里的其中一个生成器去生成验证码图片
        int index = RandomUtils.nextInt(0, pictureCaptchaGeneratorList.size() - 1);

        PictureCaptchaGenerator captchaGenerator = pictureCaptchaGeneratorList.get(index);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 生成验证码
        String captcha = captchaGenerator.generateCaptcha(entity, os);

        String deviceIdentified = SpringMvcUtils.getRequestHeaderDeviceIdentified();

        // 返回验证码的图片流
        HttpHeaders headers = new HttpHeaders();

        Object value;

        if (StringUtils.isNotEmpty(deviceIdentified)) {
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> data = new LinkedHashMap<>();

            data.put(DEFAULT_BASE_64_KEY, Base64Utils.encodeToString(os.toByteArray()));

            value = new RestResult<>(
                    "生成图片验证码成功",
                    HttpStatus.OK.value(),
                    RestResult.SUCCESS_EXECUTE_CODE,
                    data
            );
        } else {
            headers.setContentType(MediaType.IMAGE_JPEG);
            value = os.toByteArray();
        }

        os.flush();
        os.close();

        ResponseEntity<Object> result = new ResponseEntity<>(value, headers, HttpStatus.OK);

        return new GenerateCaptchaResult(result, captcha);
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return captchaParamName;
    }
}
