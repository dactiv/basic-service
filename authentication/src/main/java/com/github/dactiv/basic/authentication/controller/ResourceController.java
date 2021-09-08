package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("resource")
@Plugin(
        name = "资源管理",
        id = "resource",
        parent = "system",
        icon = "icon-attachment",
        type = ResourceType.Menu,
        sources = "Console"
)
public class ResourceController {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private MybatisPlusQueryGenerator<Resource> queryGenerator;

    @Autowired
    private PluginResourceService pluginResourceService;

    /**
     * 查找资源
     *
     * @param request http 请求
     * @return 资源实体集合
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[resource:find]')")
    @Plugin(name = "查找全部", sources = "Console")
    public List<Resource> find(HttpServletRequest request, @RequestParam(required = false) boolean mergeTree) {

        List<Resource> resourceList = authorizationService.findResources(queryGenerator.getQueryWrapperByHttpRequest(request));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
    }

    /**
     * 获取资源
     *
     * @param id 主键值
     * @return 资源实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[resource:get]')")
    @Plugin(name = "获取信息", sources = "Console")
    public Resource get(@RequestParam Integer id) {
        return authorizationService.getResource(id);
    }

    /**
     * 获取用户关联资源实体集合
     *
     * @param userId 用户主键值
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsoleUserResources")
    @Plugin(name = "获取用户资源 id 集合", sources = "Console")
    public List<Integer> getConsoleUserResources(@RequestParam Integer userId) {
        List<Resource> resourceList = authorizationService.getConsoleUserResources(userId);

        return resourceList.stream().map(Resource::getId).collect(Collectors.toList());
    }

    /**
     * 获取当前用户资源
     *
     * @param securityContext 安全上下文
     * @param mergeTree       是否合并树形 true，是 否则 false
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsolePrincipalResources")
    @Plugin(name = "获取当前用户资源", sources = "Console")
    public List<Resource> getConsolePrincipalResources(@CurrentSecurityContext SecurityContext securityContext,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) boolean mergeTree) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        List<String> sourceContains = Arrays.asList(userDetails.getType(), ResourceSource.All.toString(), ResourceSource.System.toString());

        Integer userId = Casts.cast(userDetails.getId());
        List<Resource> resourceList = authorizationService.getConsolePrincipalResources(userId, sourceContains, type);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
    }

    /**
     * 获取组资源集合
     *
     * @param groupId 组 id
     * @return 资源实体集合
     */
    @GetMapping("getGroupResource")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取组资源 id 集合", sources = "Console")
    public List<Integer> getGroupResource(@RequestParam Integer groupId) {
        List<Resource> resourceList = authorizationService.getGroupResources(groupId, null);
        return resourceList.stream().map(Resource::getId).collect(Collectors.toList());
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("syncPluginResource")
    @PreAuthorize("hasAuthority('perms[resource:sync_plugin_resource]') and isFullyAuthenticated()")
    @Plugin(name = "同步插件资源", sources = "Console", audit = true)
    public RestResult<?> syncPluginResource() {

        pluginResourceService.resubscribeAllService();

        return RestResult.of("同步数据完成");
    }
}
