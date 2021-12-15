package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.domain.entity.GroupEntity;
import com.github.dactiv.basic.authentication.service.GroupService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

import static com.github.dactiv.basic.commons.Constants.WEB_FILTER_RESULT_ID;

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
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class GroupController {

    private final GroupService groupService;

    private final MybatisPlusQueryGenerator<GroupEntity> queryGenerator;

    public GroupController(GroupService groupService, MybatisPlusQueryGenerator<GroupEntity> queryGenerator) {
        this.groupService = groupService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 获取所有用户组
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[group:find]')")
    @Plugin(name = "查询全部", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public List<GroupEntity> find(HttpServletRequest request, @RequestParam(required = false) boolean mergeTree) {

        List<GroupEntity> groupList = groupService.find(queryGenerator.getQueryWrapperByHttpRequest(request));

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
    @PreAuthorize("hasAuthority('perms[group:get]')")
    @Plugin(name = "获取信息", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public GroupEntity get(@RequestParam Integer id) {
        return groupService.get(id);
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
    @SocketMessage(WEB_FILTER_RESULT_ID)
    @PreAuthorize("hasAuthority('perms[group:save]') and isFullyAuthenticated()")
    @Plugin(name = "保存", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:authentication:group:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> save(@Valid @RequestBody GroupEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {

        boolean isNew = Objects.isNull(entity.getId());

        groupService.save(entity);

        if (isNew) {
            SocketResultHolder.get().addBroadcastSocketMessage(GroupEntity.CREATE_SOCKET_EVENT_NAME, entity);
        } else {
            SocketResultHolder.get().addBroadcastSocketMessage(GroupEntity.UPDATE_SOCKET_EVENT_NAME, entity);
        }

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
    @Plugin(name = "删除", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:authentication:group:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {

        groupService.deleteById(ids);

        SocketResultHolder.get().addBroadcastSocketMessage(GroupEntity.DELETE_SOCKET_EVENT_NAME, ids);

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
    @Plugin(name = "判断权限值是否唯一", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public boolean isAuthorityUnique(@RequestParam String authority) {
        return !groupService.lambdaQuery().eq(GroupEntity::getAuthority, authority).exists();
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
    @Plugin(name = "判断组名称是否唯一", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public boolean isNameUnique(@RequestParam String name) {
        return !groupService.lambdaQuery().eq(GroupEntity::getName, name).exists();
    }
}
