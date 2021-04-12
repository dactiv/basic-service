package com.fuyu.basic.captcha.service;

import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.support.security.feign.BasicAuthFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = "message", configuration = BasicAuthFeignConfiguration.class)
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
