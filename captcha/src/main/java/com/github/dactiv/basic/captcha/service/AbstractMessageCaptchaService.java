package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.basic.commons.feign.config.ConfigFeignClient;
import com.github.dactiv.basic.commons.feign.message.MessageFeignClient;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;

/**
 * 消息验证码发送的抽象实现
 *
 * @param <T> 消息类型实现
 * @param <C> 可过期的验证码类实现
 *
 * @author maurice
 */
public abstract class AbstractMessageCaptchaService<T extends MessageType, C extends ExpiredCaptcha>
        extends AbstractRedisCaptchaService<T, C> {

    /**
     * 配置管理服务
     */
    private final ConfigFeignClient configFeignClient;

    /**
     * 消息服务
     */
    private final MessageFeignClient messageFeignClient;

    public AbstractMessageCaptchaService(RedissonClient redissonClient,
                                         Validator validator,
                                         ConfigFeignClient configFeignClient,
                                         MessageFeignClient messageFeignClient) {
        super(redissonClient, validator);
        this.configFeignClient = configFeignClient;
        this.messageFeignClient = messageFeignClient;
    }

    @Override
    protected GenerateCaptchaResult generateCaptcha(BuildToken buildToken, T entity) {

        List<Map<String, Object>> dicList = configFeignClient.findDataDictionaries(entity.getMessageType());

        if (dicList.isEmpty()) {
            throw new ServiceException("找不到类型为:" + entity.getMessageType() + "的消息模板");
        }

        if (dicList.size() > 1) {
            throw new ServiceException("通过:" + entity.getMessageType() +
                    "找出" + dicList.size() + "条记录，并非一条记录");
        }

        String captcha = generateCaptcha();

        Map<String, Object> entry = dicList.iterator().next();

        Map<String, Object> param = createSendMessageParam(entity, entry, captcha);

        RestResult<Object> result = messageFeignClient.send(param);
        // 如果发送成记录短信验证码到 redis 中给校验备用。
        if (result.getStatus() != HttpStatus.OK.value()) {
            throw new ServiceException("消息发送失败");
        }

        return new GenerateCaptchaResult(result, captcha);
    }

    @Override
    protected boolean isMatchesFailureDeleteCaptcha() {
        return false;
    }

    /**
     * 创建消息发送参数
     *
     * @param entity  泛型实体
     * @param entry   字典内容
     * @param captcha 验证码
     *
     * @return 参数 map
     */
    protected abstract Map<String, Object> createSendMessageParam(T entity, Map<String, Object> entry, String captcha);

    /**
     * 生成验证码
     *
     * @return 验证码
     */
    protected abstract String generateCaptcha();

}
