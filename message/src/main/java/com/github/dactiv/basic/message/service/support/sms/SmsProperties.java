package com.github.dactiv.basic.message.service.support.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 亿美短信配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "message.sms.yimei")
public class SmsProperties {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 应用 id
     */
    private String applicationId;

    /**
     * 密码
     */
    private String password;

    /**
     * 发送短信 url
     */
    private String url;

    /**
     * 发送成功的字段名称
     */
    private String successFieldName = "code";

    /**
     * 响应成功的字段值
     */
    private String successFieldValue = "SUCCESS";

    /**
     * 响应数据的字段名
     */
    private String responseDataField = "data";

    /**
     * 查询余额字段名
     */
    private String balanceFieldName = "balance";

    /**
     * 创建调用 api 的基础参数
     *
     * @return 基础参数信息
     */
    public MultiValueMap<String, Object> createBaseParam() {

        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>(3);

        String timestamp = LocalDateTime.now().format(formatter);

        param.add("appId", getApplicationId());
        param.add("timestamp", timestamp);
        param.add("sign", DigestUtils.md5DigestAsHex((applicationId + password + timestamp).getBytes()).toUpperCase());

        return param;
    }
}
