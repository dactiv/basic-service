package com.github.dactiv.basic.message.service.support.sms;

import com.github.dactiv.basic.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 亿美短信渠道发送者实现
 *
 * @author maurice
 */
@SuppressWarnings("unchecked")
@Component
@RefreshScope
public class YimeiSmsChannelSender implements SmsChannelSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(YimeiSmsChannelSender.class);

    private static final String DEFAULT_SEND_SMS_API = "/simpleinter/sendSMS";

    private static final String DEFAULT_GET_BALANCE_API = "/simpleinter/getBalance";

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String getType() {
        return "yimei";
    }

    @Override
    public String getName() {
        return "亿美短信渠道";
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity) {

        MultiValueMap<String, Object> param = smsProperties.createBaseParam();

        param.add("mobiles", entity.getPhoneNumber());
        param.add("content", entity.getContent());
        param.add("customSmsId", entity.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(param, headers);

        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("对号码为[" + entity.getPhoneNumber() + "]发送短信，参数为:" + param);
            }

            ResponseEntity<String> r = restTemplate.postForEntity(smsProperties.getUrl() + DEFAULT_SEND_SMS_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    return RestResult.of(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("对号码为[" + entity.getPhoneNumber() + "]发送短信的响应结果为:" + r.getBody());
                }

                Map<String, Object> data = Casts.readValue(r.getBody(), Map.class);

                if (MapUtils.isEmpty(data)) {
                    return RestResult.of(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE);
                }

                if (data.containsKey(smsProperties.getSuccessFieldName()) && data.get(smsProperties.getSuccessFieldName()).equals(smsProperties.getSuccessFieldValue())) {
                    return new RestResult<>(
                            r.getStatusCode().getReasonPhrase(),
                            r.getStatusCode().value(),
                            String.valueOf(r.getStatusCode().value()),
                            Casts.cast(data.get(smsProperties.getResponseDataField())));
                } else {
                    return RestResult.of(
                            "执行返回的[" + smsProperties.getSuccessFieldName() + "]值为:" + data.get(smsProperties.getSuccessFieldName()) + "，不等于 " + smsProperties.getSuccessFieldValue(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            ErrorCodeException.DEFAULT_EXCEPTION_CODE
                    );
                }

            } else {

                return RestResult.of(
                        r.getStatusCode().getReasonPhrase(),
                        r.getStatusCode().value(),
                        String.valueOf(r.getStatusCode().value())
                );

            }
        } catch (Exception e) {
            return RestResult.ofException(e);
        }
    }

    @Override
    public SmsBalance getBalance() {

        MultiValueMap<String, Object> param = smsProperties.createBaseParam();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(param, headers);

        try {
            ResponseEntity<String> r = restTemplate.postForEntity(smsProperties.getUrl() + DEFAULT_GET_BALANCE_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    LOGGER.warn("通过 API " + smsProperties.getUrl() + DEFAULT_GET_BALANCE_API + " 无任何响应");
                } else {

                    Map<String, Object> data = Casts.readValue(r.getBody(), Map.class);
                    Map<String, Object> balanceMap = Casts.cast(data.get(smsProperties.getResponseDataField()));
                    BigDecimal balance = Casts.cast(balanceMap.get(smsProperties.getBalanceFieldName()), BigDecimal.class);

                    return new SmsBalance(getName(), balance);
                }
            } else {
                LOGGER.warn("通过 API " + smsProperties.getUrl() + DEFAULT_GET_BALANCE_API + " 获取不到余额信息");
            }

            return null;
        } catch (Exception e) {
            LOGGER.error("通过 API " + smsProperties.getUrl() + DEFAULT_GET_BALANCE_API + " 获取余额时，出错", e);

            return null;
        }
    }
}
