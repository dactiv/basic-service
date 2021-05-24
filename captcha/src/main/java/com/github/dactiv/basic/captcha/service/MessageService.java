package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.framework.spring.security.BasicAuthenticationConfiguration;

import com.github.dactiv.framework.commons.RestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = "message", configuration = BasicAuthenticationConfiguration.class)
public interface MessageService {

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    @PostMapping("sendMessage")
    RestResult<Map<String, Object>> sendMessage(@RequestParam Map<String, Object> request);

}
