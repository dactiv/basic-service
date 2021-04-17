package com.github.dactiv.basic.message.service;

import com.github.dactiv.framework.spring.security.feign.BasicAuthFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 认证服务
 *
 * @author maurice
 */
@FeignClient(value = "authentication", configuration = BasicAuthFeignConfiguration.class)
public interface AuthenticationService {

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     * @return 认证信息
     */
    @PostMapping("authentication/lastInfo")
    Map<String, Object> getLastAuthenticationInfo(@RequestParam("userId") Integer userId, @RequestParam("types") List<String> types);
}
