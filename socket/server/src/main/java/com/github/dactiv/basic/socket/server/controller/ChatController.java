package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.domain.meta.RecentContactMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.service.chat.MessageOperation;
import com.github.dactiv.basic.socket.server.service.chat.support.PersonMessageOperation;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 聊天控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("chat")
@Plugin(
        name = "聊天管理",
        id = "chat",
        parent = "socket-server",
        icon = "icon-message",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class ChatController {

    private final List<MessageOperation> messageOperations;

    public ChatController(ObjectProvider<MessageOperation> messageResolvers) {
        this.messageOperations = messageResolvers.orderedStream().collect(Collectors.toList());
    }

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
    @Plugin(name = "发送消息", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public RestResult<?> sendMessage(@CurrentSecurityContext SecurityContext securityContext,
                                     @RequestParam Integer recipientId,
                                     @RequestParam Integer type,
                                     @RequestParam String content) throws Exception {


        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer senderId = Casts.cast(userDetails.getId());

        MessageOperation messageOperation = getMessageResolver(type);
        GlobalMessageMeta.Message message = messageOperation.sendMessage(senderId, recipientId, content);

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
    @Plugin(name = "读取消息", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public RestResult<?> readMessage(@CurrentSecurityContext SecurityContext securityContext,
                                     @RequestBody ReadMessageRequestBody body) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer readerId = Casts.cast(userDetails.getId());
        body.setReaderId(readerId);

        if (Objects.isNull(body.getCreationTime())) {
            body.setCreationTime(new Date());
        }

        MessageOperation messageOperation = messageOperations
                .stream()
                .filter(r -> r.isSupport(body.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + body.getType().getValue() + "] 的消息解析器"));

        messageOperation.readMessage(body);

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
    @Plugin(name = "获取常用联系人", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public List<RecentContactMeta> getRecentContacts(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        PersonMessageOperation messageResolver = messageOperations
                .stream()
                .filter(r -> r.isSupport(MessageTypeEnum.CONTACT))
                .map(r -> Casts.cast(r, PersonMessageOperation.class))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + MessageTypeEnum.CONTACT.getValue() + "] 的消息解析器"));

        return messageResolver.getRecentContacts(userId);
    }

    /**
     * 获取历史消息分页
     *
     * @param securityContext 安全上下文
     * @param targetId        目标 id（对方用户 id/群聊 id）
     * @param type            目标类型
     * @param time            时间节点
     * @param pageRequest     分页请求
     *
     * @return 滚动分页结果
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("getHistoryMessagePage")
    @Plugin(name = "获取历史消息分页", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public GlobalMessagePage getHistoryMessagePage(@CurrentSecurityContext SecurityContext securityContext,
                                                   @RequestParam Integer targetId,
                                                   @RequestParam Integer type,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date time,
                                                   ScrollPageRequest pageRequest) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        MessageOperation messageOperation = getMessageResolver(type);

        return messageOperation.getHistoryMessagePage(userId, targetId, time, pageRequest);
    }

    /**
     * 获取历史消息日期集合
     *
     * @param securityContext 安全上下文
     * @param type            类型
     * @param targetId        目标 id（对方用户 id/ 群聊 id）
     *
     * @return 历史消息日期集合
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("getHistoryMessageDateList")
    @Plugin(name = "获取历史消息分页", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public List<Date> getHistoryMessageDateList(@CurrentSecurityContext SecurityContext securityContext,
                                                @RequestParam Integer type,
                                                @RequestParam Integer targetId) {

        MessageOperation messageOperation = getMessageResolver(type);

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        return messageOperation.getHistoryMessageDateList(userId, targetId);
    }

    /**
     * 获取消息操作解析者
     *
     * @param type 消息类型
     *
     * @return 消息操作解析者
     */
    private MessageOperation getMessageResolver(Integer type) {

        MessageTypeEnum messageType = ValueEnumUtils.parse(type, MessageTypeEnum.class);

        return messageOperations
                .stream()
                .filter(r -> r.isSupport(messageType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的消息解析器"));
    }

}
