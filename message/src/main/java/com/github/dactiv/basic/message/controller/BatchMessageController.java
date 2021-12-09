package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.basic.message.domain.entity.BatchMessageEntity;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

;

/**
 * tb_batch_message 的控制器
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @see BatchMessageEntity
 * @since 2021-08-22 04:45:14
 */
@RestController
@RequestMapping("batch")
@Plugin(
        name = "批量消息",
        id = "batch",
        parent = "message",
        icon = "icon-batch",
        type = ResourceType.Menu,
        sources = ResourceSource.CONSOLE_SOURCE_VALUE
)
public class BatchMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MybatisPlusQueryGenerator<?> queryGenerator;

    /**
     * 获取 table: tb_batch_message 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return 分页实体
     *
     * @see BatchMessageEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[batch_message:page]')")
    @Plugin(name = "获取分页", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public Page<BatchMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return messageService.findBatchMessagePage(
                pageRequest,
                queryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取 table: tb_batch_message 实体
     *
     * @param id 主键 ID
     *
     * @return tb_batch_message 实体
     *
     * @see BatchMessageEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[batch_message:get]')")
    @Plugin(name = "获取实体", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public BatchMessageEntity get(@RequestParam("id") Integer id) {
        return messageService.getBatchMessage(id);
    }

    /**
     * 删除 table: tb_batch_message 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see BatchMessageEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[batch_message:delete]')")
    @Plugin(name = "删除实体", sources = ResourceSource.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:message:email:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        messageService.deleteBatchMessage(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
