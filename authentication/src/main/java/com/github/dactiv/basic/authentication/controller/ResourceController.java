package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.entity.SystemUser;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceService;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
        sources = ResourceSource.CONSOLE_SOURCE_VALUE
)
public class ResourceController {


    @Autowired
    private UserService userService;

    /**
     * 查找资源
     *
     * @param mergeTree 合并树行
     *
     * @return 资源实体集合
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[resource:find]')")
    @Plugin(name = "查找全部", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public List<Resource> find(@RequestParam(required = false) boolean mergeTree,
                               @RequestParam(required = false) String applicationName,
                               @RequestParam(required = false) List<String> sources) {

        if (CollectionUtils.isEmpty(sources)) {
            sources = new LinkedList<>();
        }

        List<Resource> resourceList = userService
                .getAuthorizationService()
                .getResources(applicationName, sources.toArray(new String[0]));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
    }

    /**
     * 获取用户关联资源实体集合
     *
     * @param userId 用户主键值
     *
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsoleUserResources")
    @Plugin(name = "获取用户资源 id 集合", sources = "Console")
    public List<String> getConsoleUserResources(@RequestParam Integer userId) {
        SystemUser systemUser = userService.getConsoleUser(userId);
        Set<Map.Entry<String, List<String>>> entrySet = systemUser.getResourceMap().entrySet();
        return entrySet.stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
    }

    /**
     * 获取当前用户资源
     *
     * @param securityContext 安全上下文
     * @param mergeTree       是否合并树形 true，是 否则 false
     *
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsolePrincipalResources")
    @Plugin(name = "获取当前用户资源", sources = "Console")
    public List<Resource> getConsolePrincipalResources(@CurrentSecurityContext SecurityContext securityContext,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) boolean mergeTree) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        List<String> sourceContains = Arrays.asList(
                userDetails.getType(),
                ResourceSource.All.toString(),
                ResourceSource.System.toString()
        );

        ConsoleUser consoleUser = userService.getConsoleUser(Casts.cast(userDetails.getId(), Integer.class));

        List<Resource> resourceList = userService
                .getAuthorizationService()
                .getSystemUserResource(consoleUser, type, sourceContains);

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
     *
     * @return 资源实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[resource:get]')")
    @Plugin(name = "获取信息", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public Resource get(@RequestParam String id) {

        return userService
                .getAuthorizationService()
                .getResources(null)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("syncPluginResource")
    @Idempotent(key = "idempotent:authentication:resource:sync-plugin-resource")
    @Plugin(name = "同步插件资源", sources = ResourceSource.CONSOLE_SOURCE_VALUE, audit = true)
    @PreAuthorize("hasAuthority('perms[resource:sync_plugin_resource]') and isFullyAuthenticated()")
    public RestResult<?> syncPluginResource() {

        userService.getAuthorizationService().getPluginResourceService().resubscribeAllService();

        return RestResult.of("同步数据完成");
    }
}
