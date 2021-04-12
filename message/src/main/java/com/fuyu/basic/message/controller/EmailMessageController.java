package com.fuyu.basic.message.controller;

import com.fuyu.basic.commons.page.Page;
import com.fuyu.basic.commons.page.PageRequest;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.message.dao.entity.EmailMessage;
import com.fuyu.basic.message.service.MessageService;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>邮件消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("email/message")
@Plugin(
        name = "邮件消息",
        id = "email_message",
        parent = "message",
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class EmailMessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送邮件用户名
     */
    @Value("${spring.mail.username}")
    private String sendMailUsername;

    /**
     * 获取邮件消息分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取邮件消息分页", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[email_message:page]')")
    public Page<EmailMessage> page(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return messageService.findEmailMessagePage(pageRequest, filter);
    }

    /**
     * 根据条件获取邮件消息
     *
     * @param filter 查询条件
     * @return 邮件消息实体
     */
    @GetMapping("getByFilter")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "根据条件获取单个邮件消息", source = ResourceSource.Console)
    public EmailMessage getByFilter(@RequestParam Map<String, Object> filter) {
        return messageService.getEmailMessageByFilter(filter);
    }

    /**
     * 获取邮件消息
     *
     * @param id 邮件消息主键 ID
     * @return 邮件消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[email_message:get]')")
    @Plugin(name = "获取邮件消息实体信息", source = ResourceSource.Console)
    public EmailMessage get(@RequestParam Integer id) {
        return messageService.getEmailMessage(id);
    }

    /**
     * 保存邮件消息
     *
     * @param entity 邮件消息实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[email_message:save]')")
    @Plugin(name = "保存邮件消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid EmailMessage entity) {
        entity.setFromUser(sendMailUsername);
        messageService.saveEmailMessage(entity);
        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除邮件消息
     *
     * @param ids 邮件消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[email_message:delete]')")
    @Plugin(name = "删除邮件消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteEmailMessage(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

}
