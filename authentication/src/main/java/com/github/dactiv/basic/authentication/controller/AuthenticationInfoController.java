package com.github.dactiv.basic.authentication.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @Autowired
    private MybatisPlusQueryGenerator<AuthenticationInfo> queryGenerator;

    /**
     * 获取认证信息表分页信息
     *
     * @param page    分页请求
     * @param request http 请求
     *
     * @return 分页实体
     */
    @RequestMapping("page")
    @Plugin(name = "获取认证信息表分页", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public IPage<AuthenticationInfo> page(Page<AuthenticationInfo> page, HttpServletRequest request) {

        return authenticationService.findAuthenticationInfoPage(page, queryGenerator.getQueryWrapperFromHttpRequest(request));
    }

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     *
     * @return 认证信息
     */
    @PostMapping("lastInfo")
    public AuthenticationInfo lastInfo(@RequestParam Integer userId, @RequestParam List<String> types) {

        return authenticationService.getLastAuthenticationInfo(userId, types);
    }
}
