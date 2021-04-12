package com.fuyu.basic.authentication.controller;

import com.fuyu.basic.authentication.dao.entity.Group;
import com.fuyu.basic.authentication.service.AuthorizationService;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.commons.tree.TreeUtils;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.plugin.Plugin;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class GroupController {

    /**
     * 账户管理服务
     */
    @Autowired
    private AuthorizationService authorizationService;

    /**
     * 获取所有用户组
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[group:find]')")
    @Plugin(name = "查询全部", source = ResourceSource.Console)
    public List<Group> find(@RequestParam Map<String, Object> filter,
                            @RequestParam(required = false) List<String> sourceContains,
                            @RequestParam(required = false) boolean mergeTree) {

        if (CollectionUtils.isNotEmpty(sourceContains)) {
            filter.put("sourceContains", sourceContains);
        } else {
            filter.remove("sourceContains");
        }

        List<Group> groupList = authorizationService.findGroups(filter);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(groupList);
        } else {
            return groupList;
        }
    }

    /**
     * 获取用户的用户组集合
     *
     * @param userId    当前用户
     * @param mergeTree 是否合并树形 true，是 否则 false
     * @return 用户组实体集合
     */
    @GetMapping("getConsoleUserGroups")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取用户组信息", source = ResourceSource.Console)
    public List<Group> getConsoleUserGroups(@RequestParam Integer userId,
                                            @RequestParam(required = false) boolean mergeTree) {

        List<Group> groupList = authorizationService.getConsoleUserGroups(userId);

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
     * @return 用户组实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[group:get]')")
    @Plugin(name = "获取信息", source = ResourceSource.Console)
    public Group get(@RequestParam Integer id) {
        return authorizationService.getGroup(id);
    }


    /**
     * 保存用户组
     *
     * @param entity      用户组实体
     * @param resourceIds 资源ids
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[group:save]')")
    @Plugin(name = "保存", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid Group entity,
                                                       @RequestParam(required = false) List<Integer> resourceIds) {

        authorizationService.saveGroup(entity, resourceIds);

        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除用户组
     *
     * @param ids 主键值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[group:delete]')")
    @Plugin(name = "删除", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {

        authorizationService.deleteGroup(ids);

        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断 spring security role 的 authority 值是否唯一
     *
     * @param authority spring security role 的 authority 值
     * @return true 是，否则 false
     */
    @GetMapping("isAuthorityUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断权限值是否唯一", source = ResourceSource.Console)
    public boolean isAuthorityUnique(@RequestParam String authority) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("authorityEq", authority);
        return authorizationService.findGroups(filter).isEmpty();
    }
}
