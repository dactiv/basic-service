package com.github.dactiv.basic.message.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.message.service.MessageSender;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.security.plugin.Plugin;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息发送控制器
 *
 * @author maurice
 */
@RestController
public class SenderController {

    private static final String DEFAULT_TYPE_PARAM_NAME = "messageType";

    private final List<MessageSender> messageSenders;

    public SenderController(List<MessageSender> messageSenders) {
        this.messageSenders = messageSenders;
    }

    /**
     * 发送消息
     *
     * @param body http servlet request body
     *
     * @return 消息结果集
     */
    @PostMapping(value = "send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Idempotent(key = "idempotent:message:send:[#securityContext.authentication.details.id]")
    @PreAuthorize("hasRole('BASIC') or (hasAuthority('perms[message:send]') and isFullyAuthenticated())")
    @Plugin(name = "发送消息", id = "send", parent = "message", sources = ResourceSourceEnum.SYSTEM_SOURCE_VALUE)
    public RestResult<Object> send(@RequestBody Map<String, Object> body,
                                   @CurrentSecurityContext SecurityContext securityContext) throws Exception {

        String type = body.get(DEFAULT_TYPE_PARAM_NAME).toString();

        return getMessageService(type).send(body);
    }

    /**
     * 更具类型获取验证码服务
     *
     * @param type 消息类型
     *
     * @return 验证码服务
     */
    private MessageSender getMessageService(String type) {
        return messageSenders
                .stream()
                .filter(c -> c.getMessageType().equals(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为[ " + type + " ]的消息发送服务"));
    }

}
