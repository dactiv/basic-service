package com.github.dactiv.basic.commons.feign.config;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 配置服务的 Feign 到用接口
 *
 * @author maurice
 */
@FeignClient(value = Constants.SYS_CONFIG_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface ConfigFeignClient {

    /**
     * 获取数据字典
     *
     * @param name 字典名称
     *
     * @return 数据字典集合
     */
    @GetMapping(value = "findDataDictionaries/{name}")
    List<Map<String, Object>> findDataDictionaries(@PathVariable("name") String name);

    /**
     * 通过 token id 获取访问 token
     *
     * @param id token id
     *
     * @return 访问 token
     */
    @GetMapping("obtainAccessToken")
    Map<String, Object> obtainAccessToken(@RequestParam("id") String id);

    @GetMapping("access/crypto/getAll")
    List<Map<String, Object>> getAllAccessCrypto();
}
