package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceService;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private PluginResourceService pluginResourceService;

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
    public List<Resource> find(@RequestParam(required = false) boolean mergeTree) {

        List<Resource> resourceList = pluginResourceService.getResources(null);

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
    public Resource get(@RequestParam Integer id) {

        return pluginResourceService
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

        pluginResourceService.resubscribeAllService();

        return RestResult.of("同步数据完成");
    }
}
