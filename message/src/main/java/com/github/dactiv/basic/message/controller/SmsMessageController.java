package com.github.dactiv.basic.message.controller;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.basic.message.dao.entity.SmsMessage;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.sms.SmsBalance;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>短信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("sms/message")
@Plugin(
        name = "短信消息",
        id = "sms_message",
        parent = "message",
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class SmsMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private List<SmsChannelSender> smsChannelSenders;

    /**
     * 获取短信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @Plugin(name = "获取短信消息分页", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[sms_message:page]')")
    public Page<SmsMessage> page(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return messageService.findSmsMessagePage(pageRequest, filter);
    }

    /**
     * 根据条件获取短信消息
     *
     * @param filter 查询条件
     * @return 短信消息实体
     */
    @GetMapping("getByFilter")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "根据条件获取单个短信消息", source = ResourceSource.Console)
    public SmsMessage getByFilter(@RequestParam Map<String, Object> filter) {
        return messageService.getSmsMessageByFilter(filter);
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     * @return 短信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[sms_message:get]')")
    @Plugin(name = "获取短信消息实体信息", source = ResourceSource.Console)
    public SmsMessage get(@RequestParam Integer id) {
        return messageService.getSmsMessage(id);
    }

    /**
     * 保存短信消息
     *
     * @param entity 短信消息实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[sms_message:save]')")
    @Plugin(name = "保存短信消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid SmsMessage entity) {
        messageService.saveSmsMessage(entity);
        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[sms_message:delete]')")
    @Plugin(name = "删除短信消息实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {
        messageService.deleteSmsMessage(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return 余额实体集合
     */
    @GetMapping("balance")
    @Plugin(name = "获取短信余额", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[sms_message:balance]')")
    public List<SmsBalance> balance() {
        return smsChannelSenders.stream().map(SmsChannelSender::getBalance).collect(Collectors.toList());
    }

}
