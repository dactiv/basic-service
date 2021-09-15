package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

;

/**
 * tb_batch_message 的控制器
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @see BatchMessage
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
        sources = "Console"
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
     * @see BatchMessage
     */
    @PostMapping("page")
    @Plugin(name = "获取分页", sources = "Console")
    @PreAuthorize("hasAuthority('perms[batch_message:page]')")
    public Page<BatchMessage> page(PageRequest pageRequest, HttpServletRequest request) {
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
     * @see BatchMessage
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[batch_message:get]')")
    @Plugin(name = "获取实体", sources = "Console")
    public BatchMessage get(@RequestParam("id") Integer id) {
        return messageService.getBatchMessage(id);
    }

    /**
     * 保存 table: tb_batch_message 实体
     *
     * @param entity tb_batch_message 实体
     *
     * @see BatchMessage
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[batch_message:save]')")
    @Plugin(name = "保存实体", sources = "Console", audit = true)
    public RestResult<Integer> save(@Valid BatchMessage entity) {
        messageService.saveBatchMessage(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_batch_message 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see BatchMessage
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[batch_message:delete]')")
    @Plugin(name = "删除实体", sources = "Console", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteBatchMessage(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
