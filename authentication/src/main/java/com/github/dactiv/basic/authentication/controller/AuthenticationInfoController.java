package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("authentication/info")
public class AuthenticationInfoController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MybatisPlusQueryGenerator<?> queryGenerator;

    /**
     * 获取认证信息表分页信息
     *
     * @param pageable 分页请求
     * @param request  http 请求
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取认证信息表分页", sources = "Console")
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public Page<AuthenticationInfo> page(@PageableDefault Pageable pageable, HttpServletRequest request) {

        return authenticationService.findAuthenticationInfoPage(
                pageable,
                queryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     *
     * @return 认证信息
     */
    @GetMapping("lastInfo")
    @PreAuthorize("hasRole('BASIC')")
    public AuthenticationInfo lastInfo(@RequestParam Integer userId, @RequestParam List<String> types) {

        return authenticationService.getLastAuthenticationInfo(userId, types);
    }
}
