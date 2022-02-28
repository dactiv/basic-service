package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * 认证信息控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("operational/audit")
@Plugin(
        name = "操作审计查询",
        id = "operational-audit",
        icon = "icon-audit",
        parent = "system",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class OperationalAuditController {

    private final AuditEventRepository auditEventRepository;

    public OperationalAuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * 获取用户审计数据
     *
     * @param pageRequest 分页请求
     * @param principal   用户登陆账户
     * @param after       数据发生时间
     * @param type        审计类型
     *
     * @return 审计事件
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[audit:page]')")
    @Plugin(name = "首页展示", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<AuditEvent> page(PageRequest pageRequest,
                                  @RequestParam(required = false) String principal,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date after,
                                  @RequestParam(required = false) String type) {

        if (!PluginAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {
            throw new ServiceException("当前审计不支持分页查询");
        }

        PluginAuditEventRepository pageAuditEventRepository = Casts.cast(auditEventRepository);

        Instant instant = Objects.nonNull(after) ? after.toInstant() : null;

        return pageAuditEventRepository.findPage(pageRequest, principal, instant, type);
    }

    /**
     * 获取用户审计数据
     *
     * @param id    主键 id
     * @param after 数据发生时间
     *
     * @return 用户审计数据
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[audit:get]')")
    @Plugin(name = "查看详情", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public AuditEvent get(@RequestParam(required = false) String id,
                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date after) {

        if (!PluginAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {
            throw new ServiceException("当前审计不支持分页查询");
        }

        PluginAuditEventRepository pluginAuditEventRepository = Casts.cast(auditEventRepository);

        StringIdEntity stringIdEntity = new StringIdEntity();

        stringIdEntity.setId(id);

        return pluginAuditEventRepository.get(stringIdEntity);
    }
}
