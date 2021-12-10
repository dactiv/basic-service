package com.github.dactiv.basic.commons.feign.authentication;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 认证服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = Constants.SYS_AUTHENTICATION_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface AuthenticationFeignClient {

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     *
     * @return 认证信息
     */
    @GetMapping("info/getLastAuthenticationInfo")
    Map<String, Object> getLastAuthenticationInfo(@RequestParam("userId") Integer userId, @RequestParam("types") List<String> types);

    /**
     * 查找用户信息
     *
     * @param filter 过滤条件
     *
     * @return 用户信息集合
     */
    @PostMapping("member/user/find")
    List<Map<String, Object>> findMemberUser(@RequestBody Map<String, Object> filter);

    /**
     * 获取系统用户信息
     *
     * @param id 用户 id
     *
     * @return 系统用户信息
     */
    @GetMapping("console/user/get")
    Map<String, Object> getConsoleUser(@RequestParam("id") Integer id);
}
