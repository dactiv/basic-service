package com.github.dactiv.basic.socket.server.service.chat;

import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.ChatConfig;
import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.GlobalMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.basic.socket.server.receiver.ReadMessageReceiver;
import com.github.dactiv.basic.socket.server.service.chat.resolver.MessageResolver;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.minio.MinioTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dactiv.basic.commons.SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 聊天业务逻辑服务
 *
 * @author maurice.chen
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatService {

    /**
     * 聊天信息事件名称
     */
    public static final String CHAT_MESSAGE_EVENT_NAME = "chat_message";
    /**
     * 聊天信息读取事件名称
     */
    public static final String CHAT_READ_MESSAGE_EVENT_NAME = "chat_read_message";

    // FIXME 这个东西存在没意义
    private final AmqpTemplate amqpTemplate;

    private final List<MessageResolver> messageResolvers;

    public ChatService(AmqpTemplate amqpTemplate, ObjectProvider<MessageResolver> messageResolvers) {
        this.amqpTemplate = amqpTemplate;
        this.messageResolvers = messageResolvers.orderedStream().collect(Collectors.toList());
    }

    /**
     * 获取消息分页
     *
     * @param userId      用户 id
     * @param type 目标类型
     * @param targetId    目标 id（对方用户 id/ 群聊 id）
     * @param pageRequest 分页请求
     *
     * @return 全局消息分页
     */
    public GlobalMessagePage getHistoryMessagePage(Integer userId,
                                                   Integer type,
                                                   Integer targetId,
                                                   Date time,
                                                   ScrollPageRequest pageRequest) {

        MessageTypeEnum messageType = ValueEnumUtils.parse(type, MessageTypeEnum.class);

        MessageResolver messageResolver = messageResolvers
                .stream()
                .filter(r -> r.isSupport(messageType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的消息解析器"));

        return messageResolver.getHistoryMessagePage(userId, targetId, time, pageRequest);
    }

    /**
     * 获取历史消息日期集合
     *
     * @param userId 用户 id
     * @param type 目标类型
     * @param targetId 目标 id（对方用户 id/ 群聊 id）
     *
     * @return 历史消息日期集合
     */
    public List<Date> getHistoryMessageDateList(Integer userId, Integer type, Integer targetId) {
        MessageTypeEnum messageType = ValueEnumUtils.parse(type, MessageTypeEnum.class);

        MessageResolver messageResolver = messageResolvers
                .stream()
                .filter(r -> r.isSupport(messageType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的消息解析器"));

        return messageResolver.getHistoryMessageDateList(userId, targetId);
    }

    /**
     * 发送消息
     *
     * @param senderId    发送人用户 id
     * @param recipientId 收信人用户 id
     * @param content     消息内容
     */
    @SocketMessage
    @Concurrent(
            value = "socket:chat:send:type:[T(Math).min(#senderId, #recipientId)]_[T(Math).max(#senderId, #recipientId)]",
            exception = "请不要过快的发送消息"
    )
    public GlobalMessageMeta.Message sendMessage(Integer senderId, Integer type, Integer recipientId, String content) throws Exception {

        MessageTypeEnum messageType = ValueEnumUtils.parse(type, MessageTypeEnum.class);

        MessageResolver messageResolver = messageResolvers
                .stream()
                .filter(r -> r.isSupport(messageType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的消息解析器"));

        return messageResolver.sendMessage(senderId, recipientId, content);
    }

    /**
     * 读取信息
     *
     * @param body 读取消息 request body
     */
    public void readMessage(ReadMessageRequestBody body) throws Exception {

        MessageResolver messageResolver = messageResolvers
                .stream()
                .filter(r -> r.isSupport(body.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + body.getType().getValue() + "] 的消息解析器"));

        messageResolver.readMessage(body.getSenderId(), body.getRecipientId(), body.getMessageIds());

        amqpTemplate.convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                ReadMessageReceiver.DEFAULT_QUEUE_NAME,
                body
        );
    }


}
