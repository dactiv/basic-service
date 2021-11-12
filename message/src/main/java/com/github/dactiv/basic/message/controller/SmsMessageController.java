package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.sms.SmsBalance;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>短信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("sms")
@Plugin(
        name = "短信消息",
        id = "sms",
        parent = "message",
        icon = "icon-sms",
        type = ResourceType.Menu,
        sources = ResourceSource.CONSOLE_SOURCE_VALUE
)
public class SmsMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private List<SmsChannelSender> smsChannelSenders;

    @Autowired
    private MybatisPlusQueryGenerator<SmsMessage> queryGenerator;

    /**
     * 获取短信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[sms:page]')")
    @Plugin(name = "获取短信消息分页", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public Page<SmsMessage> page(PageRequest pageRequest, HttpServletRequest request) {
        return messageService.findSmsMessagePage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     *
     * @return 短信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[sms:get]')")
    @Plugin(name = "获取短信消息实体信息", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    public SmsMessage get(@RequestParam Integer id) {
        return messageService.getSmsMessage(id);
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[sms:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除短信消息实体", sources = ResourceSource.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:message:sms:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        messageService.deleteSmsMessage(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return 余额实体集合
     */
    @GetMapping("balance")
    @Plugin(name = "获取短信余额", sources = ResourceSource.CONSOLE_SOURCE_VALUE)
    @PreAuthorize("hasAuthority('perms[sms:balance]')")
    public List<SmsBalance> balance() {
        return smsChannelSenders.stream().map(SmsChannelSender::getBalance).collect(Collectors.toList());
    }

}
