package com.github.dactiv.basic.authentication.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.DiscoveryPluginResourceService;
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
import javax.validation.Valid;
import java.util.Arrays;
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
        type = ResourceType.Menu,
        sources = "Console"
)
public class ResourceController {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DiscoveryPluginResourceService discoveryPluginResourceService;

    @Autowired
    private MybatisPlusQueryGenerator<Resource> queryGenerator;

    /**
     * 查找资源
     *
     * @param request http 请求
     *
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
     *
     * @return 资源实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[resource:get]')")
    @Plugin(name = "获取信息", sources = "Console")
    public Resource get(@RequestParam Integer id) {
        return authorizationService.getResource(id);
    }

    /**
     * 保存资源
     *
     * @param entity 资源实体
     *
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[resource:save]')")
    @Plugin(name = "保存", sources = "Console", audit = true)
    public RestResult.Result<Integer> save(@Valid Resource entity) {

        entity.setType(ResourceType.Menu.toString());

        authorizationService.saveResource(entity);

        return RestResult.build("保存成功", entity.getId());
    }

    /**
     * 删除资源
     *
     * @param ids 主键值集合
     *
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[resource:delete]')")
    @Plugin(name = "删除", sources = "Console", audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {

        authorizationService.deleteResources(ids);

        return RestResult.build("删除" + ids.size() + "条记录成功");
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
    @Plugin(name = "获取用户资源", sources = "Console")
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
     *
     * @return 资源实体集合
     */
    @GetMapping("getGroupResource")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "根据组 id 获取资源", sources = "Console")
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
     *
     * @return true 是，否则 false
     */
    @GetMapping("isCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断唯一识别值是否唯一", sources = "Console")
    public boolean isCodeUnique(@RequestParam String code) {

        return authorizationService.findResources(Wrappers.<Resource>lambdaQuery().eq(Resource::getCode, code)).isEmpty();
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("syncPluginResource")
    @PreAuthorize("hasAuthority('perms[resource:syncPluginResource]')")
    @Plugin(name = "同步插件资源", sources = "Console", audit = true)
    public RestResult.Result<?> syncPluginResource() {

        discoveryPluginResourceService.cleanExceptionServices();
        discoveryPluginResourceService.syncPluginResource();

        return RestResult.build("同步数据完成");
    }
}
