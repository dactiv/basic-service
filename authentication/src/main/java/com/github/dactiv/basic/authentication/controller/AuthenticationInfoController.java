package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.basic.authentication.service.AuthenticationInfoService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
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
@Plugin(
        name = "用户登陆信息查询",
        parent = "system",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
        type = ResourceType.Menu,
        icon = "icon-sign-out"
)
public class AuthenticationInfoController {

    private final AuthenticationInfoService authenticationInfoService;

    private final MybatisPlusQueryGenerator<?> queryGenerator;

    public AuthenticationInfoController(AuthenticationInfoService authenticationInfoService,
                                        MybatisPlusQueryGenerator<?> queryGenerator) {
        this.authenticationInfoService = authenticationInfoService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取认证信息表分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    @Plugin(name = "首页展示", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<AuthenticationInfoEntity> page(PageRequest pageRequest, HttpServletRequest request) {

        return authenticationInfoService.findPage(
                pageRequest,
                queryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取认证信息实体
     *
     * @param id 主键值
     *
     * @return 认证信息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[authentication_info:get]')")
    @Plugin(name = "查看详情", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public AuthenticationInfoEntity get(@RequestParam Integer id) {
        return authenticationInfoService.get(id);
    }

    /**
     * 获取最后一次认证信息
     *
     * @param userId 用户 id
     * @param types  认证类型
     *
     * @return 认证信息
     */
    @PreAuthorize("hasRole('BASIC')")
    @GetMapping("getLastAuthenticationInfo")
    public AuthenticationInfoEntity getLastAuthenticationInfo(@RequestParam Integer userId, @RequestParam List<String> types) {
        return authenticationInfoService.getLastAuthenticationInfo(userId, types);
    }
}
