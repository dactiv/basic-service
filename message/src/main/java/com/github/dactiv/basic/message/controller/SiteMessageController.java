package com.github.dactiv.basic.message.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.spring.web.RestResult;
import com.github.dactiv.basic.message.dao.entity.SiteMessage;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("site/message")
@Plugin(
        name = "站内信消息",
        id = "site_message",
        parent = "message",
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class SiteMessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取站内信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[site_message:page]') || hasRole('ORDINARY')")
    @Plugin(name = "获取站内信消息分页", source = ResourceSource.All)
    public Page<SiteMessage> page(PageRequest pageRequest,
                                  @RequestParam Map<String, Object> filter,
                                  @RequestParam(required = false) List<String> typeContain,
                                  @CurrentSecurityContext SecurityContext securityContext) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        if (userDetails.getType().equals(ResourceSource.Mobile.toString()) || userDetails.getType().equals(ResourceSource.UserCenter.toString())) {
            filter.put("toUserIdEq", userDetails.getId());
        }

        if (!CollectionUtils.isEmpty(typeContain)) {
            filter.put("typeContain", typeContain);
        } else {
            filter.put("typeContain", new ArrayList<>());
        }

        return messageService.findSiteMessagePage(pageRequest, filter);
    }

    /**
     * 按类型分组获取站内信未读数量
     *
     * @param securityContext 安全上下文
     * @return 按类型分组的未读数量
     */
    @GetMapping("unreadQuantity")
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "按类型分组获取站内信未读数量", source = {ResourceSource.Mobile, ResourceSource.UserCenter})
    public List<Map<String, Object>> unreadQuantity(@CurrentSecurityContext SecurityContext securityContext) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer id = Casts.cast(userDetails.getId());

        return messageService.countSiteMessageUnreadQuantity(id);
    }

    /**
     * 阅读站内信
     *
     * @param types           站内信类型
     * @param securityContext 安全上下文
     * @return 消息结果集
     */
    @PostMapping("read")
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "阅读站内信", source = {ResourceSource.Mobile, ResourceSource.UserCenter}, audit = true)
    public RestResult.Result<Map<String, Object>> read(@RequestParam(required = false) List<String> types,
                                                       @CurrentSecurityContext SecurityContext securityContext) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer id = Casts.cast(userDetails.getId());

        messageService.readSiteMessage(types, id);

        return RestResult.build("阅读成功");
    }

    /**
     * 根据条件获取站内信消息
     *
     * @param filter 查询条件
     * @return 站内信消息实体
     */
    @GetMapping("getByFilter")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "根据条件获取单个站内信消息", source = ResourceSource.Console)
    public SiteMessage getByFilter(@RequestParam Map<String, Object> filter) {
        return messageService.getSiteMessageByFilter(filter);
    }

    /**
     * 获取站内信消息
     *
     * @param id 站内信消息主键 ID
     * @return 站内信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[site_message:get]')")
    @Plugin(name = "获取站内信消息实体信息", source = ResourceSource.Console)
    public SiteMessage get(@RequestParam Integer id) {
        return messageService.getSiteMessage(id);
    }

    /**
     * 保存站内信消息
     *
     * @param entity 站内信消息实体
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[site_message:save]')")
    @Plugin(name = "保存站内信消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid SiteMessage entity) {
        messageService.saveSiteMessage(entity);
        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除站内信消息
     *
     * @param ids 站内信消息主键 ID 值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[site_message:delete]')")
    @Plugin(name = "删除站内信消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteSiteMessage(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

}
