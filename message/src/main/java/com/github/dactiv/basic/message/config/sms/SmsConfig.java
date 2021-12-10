package com.github.dactiv.basic.message.config.sms;


import com.github.dactiv.basic.message.config.sms.yimei.YimeiConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dactiv.message.sms")
public class SmsConfig {
    /**
     * 渠道商
     */
    private String channel;

    /**
     * 亿美短信配置
     */
    private YimeiConfig yimei;
}
