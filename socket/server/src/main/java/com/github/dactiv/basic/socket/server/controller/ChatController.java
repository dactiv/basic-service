package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.service.chat.data.BasicMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.ScrollPage;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @param recipientId     收信人用户 id
     * @param content         消息内容
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
     * @param body            读取消息 request body
     *
     * @return rest 结果集
     */
    @PostMapping("readMessage")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> readMessage(@CurrentSecurityContext SecurityContext securityContext,
                                     @RequestBody ReadMessageRequestBody body) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer readUserId = Casts.cast(userDetails.getId());
        body.setRecipientId(readUserId);

        chatService.readMessage(body);

        return RestResult.ofSuccess("读取信息成功");
    }

    /**
     * 获取常用联系人 id 集合
     *
     * @param securityContext 安全上下文
     *
     * @return 常用联系人 id 集合
     */
    @GetMapping("getRecentContacts")
    @PreAuthorize("isAuthenticated()")
    public List<Integer> getRecentContacts(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        return chatService.getRecentContacts(userId);
    }

    /**
     * 获取未读消息
     *
     * @param securityContext 安全上下文
     *
     * @return 未读消息集合
     */
    @GetMapping("getUnreadMessages")
    @PreAuthorize("isAuthenticated()")
    public List<ContactMessage<BasicMessage.UserMessageBody>> getUnreadMessages(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        return chatService.getUnreadMessages(userId);
    }

    /**
     * 获取历史消息分页
     *
     * @param securityContext 安全上下文
     * @param targetId 目标用户 id（对方用户 id）
     * @param time 时间节点
     * @param pageRequest 分页请求
     *
     * @return 滚动分页结果
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("getHistoryMessagePage")
    public ScrollPage<GlobalMessage.FileMessage> getHistoryMessagePage(@CurrentSecurityContext SecurityContext securityContext,
                                                                       @RequestParam Integer targetId,
                                                                       @RequestParam Integer time,
                                                                       ScrollPageRequest pageRequest) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        return chatService.getHistoryMessagePage(userId, targetId, time, pageRequest);
    }

}
