package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.sms.SmsBalance;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
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
        sources = "Console"
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
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取短信消息分页", sources = "Console")
    @PreAuthorize("hasAuthority('perms[sms:page]')")
    public Page<SmsMessage> page(PageRequest pageRequest, HttpServletRequest request) {
        return messageService.findSmsMessagePage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     * @return 短信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[sms:get]')")
    @Plugin(name = "获取短信消息实体信息", sources = "Console")
    public SmsMessage get(@RequestParam Integer id) {
        return messageService.getSmsMessage(id);
    }

    /**
     * 保存短信消息
     *
     * @param entity 短信消息实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[sms:save]')")
    @Plugin(name = "保存短信消息实体", sources = "Console", audit = true)
    public RestResult<Integer> save(@Valid SmsMessage entity) {
        messageService.saveSmsMessage(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[sms:delete]')")
    @Plugin(name = "删除短信消息实体", sources = "Console", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteSmsMessage(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return 余额实体集合
     */
    @GetMapping("balance")
    @Plugin(name = "获取短信余额", sources = "Console")
    @PreAuthorize("hasAuthority('perms[sms:balance]')")
    public List<SmsBalance> balance() {
        return smsChannelSenders.stream().map(SmsChannelSender::getBalance).collect(Collectors.toList());
    }

}
