package com.github.dactiv.basic.message.service.support.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.spring.web.RestResult;
import com.github.dactiv.basic.message.dao.entity.SmsMessage;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value("${spring.sms.yimei.id}")
    private String applicationId;

    @Value("${spring.sms.yimei.password}")
    private String password;

    @Value("${spring.sms.yimei.url}")
    private String url;

    @Value("${spring.sms.yimei.success.field:code}")
    private String successFieldName;

    @Value("${spring.sms.yimei.balance.field:balance}")
    private String balanceFieldName;

    @Value("${spring.sms.yimei.success.value:SUCCESS}")
    private String successFieldValue;

    @Value("${spring.sms.yimei.response.data.field:data}")
    private String responseDataField;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String getType() {
        return "yimei";
    }

    @Override
    public String getName() {
        return "亿美短信渠道";
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessage entity) {

        MultiValueMap<String, Object> param = createBaseParam();

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

            ResponseEntity<String> r = restTemplate.postForEntity(url + DEFAULT_SEND_SMS_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    return new RestResult<>(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            RestResult.ERROR_EXECUTE_CODE);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("对号码为[" + entity.getPhoneNumber() + "]发送短信的响应结果为:" + r.getBody());
                }

                Map<String, Object> data = objectMapper.readValue(r.getBody(), Map.class);

                if (MapUtils.isEmpty(data)) {
                    return new RestResult<>(
                            "返回的数据为 null",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            RestResult.ERROR_EXECUTE_CODE);
                }

                if (data.containsKey(successFieldName) && data.get(successFieldName).equals(successFieldValue)) {
                    return new RestResult<>(
                            r.getStatusCode().getReasonPhrase(),
                            r.getStatusCode().value(),
                            String.valueOf(r.getStatusCode().value()),
                            Casts.cast(data.get(responseDataField)));
                } else {
                    return new RestResult<>(
                            "执行返回的[" + successFieldName + "]值为:" + data.get(successFieldName) + "，不等于 " + successFieldValue,
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            RestResult.ERROR_EXECUTE_CODE);
                }

            } else {

                return new RestResult<>(
                        r.getStatusCode().getReasonPhrase(),
                        r.getStatusCode().value(),
                        String.valueOf(r.getStatusCode().value()));

            }
        } catch (Exception e) {
            return new RestResult<>(
                    "执行时候出现异常:" + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    RestResult.ERROR_EXECUTE_CODE);
        }
    }

    @Override
    public SmsBalance getBalance() {

        MultiValueMap<String, Object> param = createBaseParam();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(param, headers);

        try {
            ResponseEntity<String> r = restTemplate.postForEntity(url + DEFAULT_GET_BALANCE_API, request, String.class);

            if (r.getStatusCode() == HttpStatus.OK) {

                if (r.getBody() == null) {
                    LOGGER.warn("通过 API " + url + DEFAULT_GET_BALANCE_API + " 无任何响应");
                } else {

                    Map<String, Object> data = objectMapper.readValue(r.getBody(), Map.class);
                    Map<String, Object> balanceMap = Casts.cast(data.get(responseDataField));
                    BigDecimal balance = Casts.cast(balanceMap.get(balanceFieldName), BigDecimal.class);

                    return new SmsBalance(getName(), balance);
                }
            } else {
                LOGGER.warn("通过 API " + url + DEFAULT_GET_BALANCE_API + " 获取不到余额信息");
            }

            return null;
        } catch (Exception e) {
            LOGGER.error("通过 API " + url + DEFAULT_GET_BALANCE_API + " 获取余额时，出错", e);

            return null;
        }
    }

    private MultiValueMap<String, Object> createBaseParam() {

        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>(16);

        String timestamp = LocalDateTime.now().format(formatter);

        param.add("appId", applicationId);
        param.add("timestamp", timestamp);
        param.add("sign", DigestUtils.md5DigestAsHex((applicationId + password + timestamp).getBytes()).toUpperCase());

        return param;
    }
}
