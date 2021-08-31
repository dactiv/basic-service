package com.github.dactiv.basic.message.service;

import com.github.dactiv.framework.spring.security.authentication.service.feign.AuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 认证服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = "authentication", configuration = AuthenticationConfiguration.class)
public interface AuthenticationService {

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     * @return 认证信息
     */
    @PostMapping("info/getLastAuthenticationInfo")
    Map<String, Object> getLastAuthenticationInfo(@RequestParam("userId") Integer userId, @RequestParam("types") List<String> types);

    /**
     * 查找用户信息
     *
     * @param filter 过滤条件
     *
     * @return 用户信息集合
     */
    @PostMapping("member/user/find")
    List<Map<String, Object>> findMemberUser(@RequestBody Map<String,Object> filter);
}
