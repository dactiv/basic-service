package com.github.dactiv.basic.authentication.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户用户组控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("group")
@Plugin(
        name = "组管理",
        id = "group",
        parent = "system",
        icon = "icon-group",
        type = ResourceType.Menu,
        sources = ResourceSource.CONSOLE_SOURCE_VALUE
)
public class GroupController {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private MybatisPlusQueryGenerator<Group> queryGenerator;

    /**
     * 获取所有用户组
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[group:find]')")
    @Plugin(name = "查询全部", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public List<Group> find(HttpServletRequest request, @RequestParam(required = false) boolean mergeTree) {

        List<Group> groupList = authorizationService.findGroups(queryGenerator.getQueryWrapperByHttpRequest(request));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(groupList);
        } else {
            return groupList;
        }
    }

    /**
     * 获取用户组
     *
     * @param id 主键值
     *
     * @return 用户组实体
     */
    @GetMapping("get")
    @Plugin(name = "获取信息", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    @PreAuthorize("hasAuthority('perms[group:get]')")
    public Group get(@RequestParam Integer id) {
        return authorizationService.getGroup(id);
    }

    /**
     * 保存用户组
     *
     * @param entity          用户组实体
     * @param securityContext 安全上下文
     *
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[group:save]') and isFullyAuthenticated()")
    @Plugin(name = "保存", sources = ResourceSource.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:authentication:group:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> save(@Valid @RequestBody Group entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        authorizationService.saveGroup(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除用户组
     *
     * @param ids 主键值集合
     *
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[group:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除", sources = ResourceSource.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:authentication:group:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {

        authorizationService.deleteGroup(ids);

        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断 spring security role 的 authority 值是否唯一
     *
     * @param authority spring security role 的 authority 值
     *
     * @return true 是，否则 false
     */
    @GetMapping("isAuthorityUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断权限值是否唯一", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public boolean isAuthorityUnique(@RequestParam String authority) {

        return authorizationService.findGroups(Wrappers.<Group>lambdaQuery().eq(Group::getAuthority, authority)).isEmpty();
    }

    /**
     * 判断组名称是否唯一
     *
     * @param name 组名称
     *
     * @return true 唯一，否则 false
     */
    @GetMapping("isNameUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断组名称是否唯一", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public boolean isNameUnique(@RequestParam String name) {
        return authorizationService.findGroups(Wrappers.<Group>lambdaQuery().eq(Group::getName, name)).isEmpty();
    }
}
