package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送消息
     *
     * @param securityContext 安全上下文（发送人信息）
     * @param recipientId 收信人用户 id
     * @param content 消息内容
     *
     * @return rest 结果集
     */
    @PostMapping("sendMessage")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> sendMessage(@CurrentSecurityContext SecurityContext securityContext,
                                     @RequestParam Integer recipientId,
                                     @RequestParam String content) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer senderId = Casts.cast(userDetails.getId());
        GlobalMessage.Message message = chatService.sendMessage(senderId, recipientId, content);

        return RestResult.ofSuccess("发送消息成功", message);
    }

    /**
     * 读取消息
     *
     * @param securityContext 安全上下文（读取信息人信息）
     * @param body 读取消息 request body
     *
     * @return rest 结果集
     */
    @PostMapping("readMessage")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> readMessage(@CurrentSecurityContext SecurityContext securityContext,
                                     @RequestBody ReadMessageRequestBody body) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer recipientId = Casts.cast(userDetails.getId());
        body.setRecipientId(recipientId);

        chatService.readMessage(body);

        return RestResult.ofSuccess("读取信息成功");
    }

}
