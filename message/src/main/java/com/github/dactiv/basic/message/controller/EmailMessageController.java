package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
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
import java.util.List;

/**
 * <p>邮件消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("email")
@Plugin(
        name = "邮件消息",
        id = "email",
        parent = "message",
        icon = "icon-email",
        type = ResourceType.Menu,
        sources = "Console"
)
public class EmailMessageController {

    @Autowired
    private AttachmentMessageService messageService;

    @Autowired
    private MybatisPlusQueryGenerator<EmailMessage> queryGenerator;

    /**
     * 获取邮件消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取邮件消息分页", sources = "Console")
    @PreAuthorize("hasAuthority('perms[email:page]')")
    public Page<EmailMessage> page(PageRequest pageRequest, HttpServletRequest request) {
        return messageService.findEmailMessagePage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取邮件消息
     *
     * @param id 邮件消息主键 ID
     * @return 邮件消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[email:get]')")
    @Plugin(name = "获取邮件消息实体信息", sources = "Console")
    public EmailMessage get(@RequestParam Integer id) {
        return messageService.getEmailMessage(id);
    }

    /**
     * 删除邮件消息
     *
     * @param ids 邮件消息主键 ID 值集合
     */
    @PostMapping("delete")
    @Plugin(name = "删除邮件消息实体", sources = "Console", audit = true)
    @PreAuthorize("hasAuthority('perms[email:delete]') and isFullyAuthenticated()")
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteEmailMessage(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
