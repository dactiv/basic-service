package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证信息控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("authentication/info")
public class AuthenticationInfoController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 获取认证信息表分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @RequestMapping("page")
    @Plugin(name = "获取认证信息表分页", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public Page<AuthenticationInfo> page(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return authenticationService.findAuthenticationInfoPage(pageRequest, filter);
    }

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     * @return 认证信息
     */
    @PostMapping("lastInfo")
    public AuthenticationInfo lastInfo(@RequestParam Integer userId, @RequestParam List<String> types) {

        PageRequest pageRequest = new PageRequest(0, 1);

        Map<String, Object> filter = new LinkedHashMap<>(pageRequest.getOffsetMap());

        filter.put("userIdEq", userId);
        filter.put("typeContain", types);

        return authenticationService.getAuthenticationInfoByFilter(filter);
    }
}
