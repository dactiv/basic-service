package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.feign.BasicAuthFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = "message", configuration = BasicAuthFeignConfiguration.class)
public interface MessageService {

    String DEFAULT_TYPE_NAME = "type";

    String DEFAULT_MESSAGE_TYPE_KEY = "messageType";

    String DEFAULT_MESSAGE_TYPE_VALUE = "siteMessage";

    String DEFAULT_MESSAGES_KEY = "messages";

    /**
     * 发送消息
     *
     * @param request 请求参数
     *
     * @return rest 结果集
     */
    @PostMapping("sendMessage")
    RestResult<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request);

}
