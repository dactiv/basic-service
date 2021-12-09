package com.github.dactiv.basic.captcha.service.picture;

import com.github.dactiv.basic.captcha.service.AbstractRedisCaptchaService;
import com.github.dactiv.basic.captcha.service.BuildToken;
import com.github.dactiv.basic.captcha.service.ExpiredCaptcha;
import com.github.dactiv.basic.captcha.service.GenerateCaptchaResult;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import java.io.ByteArrayOutputStream;
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

    private final List<PictureCaptchaGenerator> pictureCaptchaGenerators;

    private final PictureCaptchaProperties properties;

    public PictureCaptchaService(RedissonClient redissonClient,
                                 @Qualifier("mvcValidator") @Autowired(required = false) Validator validator,
                                 List<PictureCaptchaGenerator> pictureCaptchaGenerators,
                                 PictureCaptchaProperties properties) {
        super(redissonClient, validator);
        this.pictureCaptchaGenerators = pictureCaptchaGenerators;
        this.properties = properties;
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return properties.getCaptchaExpireTime();
    }

    @Override
    protected GenerateCaptchaResult generateCaptcha(BuildToken buildToken, PictureEntity entity) throws Exception {
        // 随机抽取验证码生成器集合里的其中一个生成器去生成验证码图片
        int index = RandomUtils.nextInt(0, pictureCaptchaGenerators.size() - 1);

        PictureCaptchaGenerator captchaGenerator = pictureCaptchaGenerators.get(index);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 生成验证码
        String captcha = captchaGenerator.generateCaptcha(entity, os);

        String deviceIdentified = SpringMvcUtils.getRequestHeaderDeviceIdentified();

        // 返回验证码的图片流
        HttpHeaders headers = new HttpHeaders();

        Object value;

        if (StringUtils.isNotBlank(deviceIdentified)) {
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
        return properties.getCaptchaParamName();
    }
}
