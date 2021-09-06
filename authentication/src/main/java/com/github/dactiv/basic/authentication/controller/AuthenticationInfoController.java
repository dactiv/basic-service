package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 认证信息控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("info")
public class AuthenticationInfoController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MybatisPlusQueryGenerator<?> queryGenerator;

    /**
     * 获取认证信息表分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取认证信息表分页", parent = "audit", sources = "Console")
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public Page<AuthenticationInfo> page(PageRequest pageRequest, HttpServletRequest request) {

        return authenticationService.findAuthenticationInfoPage(
                pageRequest,
                queryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     * @return 认证信息
     */
    @GetMapping("getLastAuthenticationInfo")
    @PreAuthorize("hasRole('BASIC')")
    public AuthenticationInfo getLastAuthenticationInfo(@RequestParam Integer userId, @RequestParam List<String> types) {

        return authenticationService.getLastAuthenticationInfo(userId, types);
    }
}
