package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.service.feign.AuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = "message", configuration = AuthenticationConfiguration.class)
public interface MessageService {

    /**
     * 默认的消息类型 key 名称
     */
    String DEFAULT_MESSAGE_TYPE_KEY = "messageType";

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    @PostMapping("send")
    RestResult<Map<String, Object>> send(@RequestBody Map<String, Object> request);

}
