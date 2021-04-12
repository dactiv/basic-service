package com.fuyu.basic.authentication.controller;

import com.fuyu.basic.authentication.dao.entity.Resource;
import com.fuyu.basic.authentication.service.AuthorizationService;
import com.fuyu.basic.authentication.service.DiscoveryPluginResourceService;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.commons.tree.TreeUtils;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.plugin.Plugin;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class ResourceController {

    /**
     * 账户管理服务
     */
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DiscoveryPluginResourceService discoveryPluginResourceService;

    /**
     * 查找资源
     *
     * @param filter 过滤条件
     * @return 资源实体集合
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[resource:find]')")
    @Plugin(name = "查找全部", source = ResourceSource.Console)
    public List<Resource> find(@RequestParam Map<String, Object> filter,
                               @RequestParam(required = false) List<String> sourceContains,
                               @RequestParam(required = false) boolean mergeTree) {

        if (CollectionUtils.isNotEmpty(sourceContains)) {
            filter.put("sourceContains", sourceContains);
        } else {
            filter.remove("sourceContains");
        }

        List<Resource> resourceList = authorizationService.findResources(filter);

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
    @Plugin(name = "获取信息", source = ResourceSource.Console)
    public Resource get(@RequestParam Integer id) {
        return authorizationService.getResource(id);
    }

    /**
     * 保存资源
     *
     * @param entity 资源实体
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[resource:save]')")
    @Plugin(name = "保存", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid Resource entity) {

        entity.setType(ResourceType.Menu.toString());

        authorizationService.saveResource(entity);

        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除资源
     *
     * @param ids 主键值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[resource:delete]')")
    @Plugin(name = "删除", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {

        authorizationService.deleteResources(ids);

        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取用户关联资源实体集合
     *
     * @param userId 用户主键值
     * @return 资源实体集合
     */
    @GetMapping("getConsoleUserResources")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取用户资源", source = ResourceSource.Console)
    public List<Resource> getConsoleUserResources(@RequestParam Integer userId, @RequestParam(required = false) boolean mergeTree) {
        List<Resource> resourceList = authorizationService.getConsoleUserResources(userId);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
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
    @Plugin(name = "获取当前用户资源", source = ResourceSource.Console)
    public List<Resource> getConsolePrincipalResources(@CurrentSecurityContext SecurityContext securityContext,
                                                       @RequestParam Map<String, Object> filter,
                                                       @RequestParam(required = false) boolean mergeTree) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        filter.put("sourceContains", Arrays.asList(
                userDetails.getType(),
                ResourceSource.All.toString(),
                ResourceSource.System)
        );

        Integer userId = Casts.cast(userDetails.getId());
        List<Resource> resourceList = authorizationService.getConsolePrincipalResources(userId, filter);

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
    @Plugin(name = "根据组 id 获取资源", source = ResourceSource.Console)
    public List<Resource> getGroupResource(@RequestParam Integer groupId, @RequestParam(required = false) boolean mergeTree) {
        List<Resource> resourceList = authorizationService.getGroupResources(groupId);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
    }

    /**
     * 判断唯一识别值是否唯一
     *
     * @param code 唯一识别值
     * @return true 是，否则 false
     */
    @GetMapping("isCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断唯一识别值是否唯一", source = ResourceSource.Console)
    public boolean isCodeUnique(@RequestParam String code) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("codeEq", code);
        return authorizationService.findResources(filter).isEmpty();
    }

    @PostMapping("syncPluginResource")
    @PreAuthorize("hasAuthority('perms[resource:syncPluginResource]')")
    @Plugin(name = "同步插件资源", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> syncPluginResource() {

        discoveryPluginResourceService.cleanExceptionServices();
        discoveryPluginResourceService.syncPluginResource();

        return RestResult.build("同步数据完成");
    }
}
