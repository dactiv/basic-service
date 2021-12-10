package com.github.dactiv.basic.socket.server.service.message.support;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.client.domain.MultipleUnicastMessage;
import com.github.dactiv.basic.socket.client.domain.SocketMessage;
import com.github.dactiv.basic.socket.server.service.message.AbstractMessageSender;
import com.github.dactiv.basic.socket.server.service.message.MessageSender;
import com.github.dactiv.framework.commons.Casts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 单数据循环单播 socket 消息发送实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class MultipleUnicastMessageSender implements MessageSender {

    @Override
    public void sendMessage(SocketMessage<?> socketMessage, SocketIOServer socketIOServer) {
        MultipleUnicastMessage<?> message = Casts.cast(socketMessage);

        List<ClientOperations> clientOperations = message
                .getDeviceIdentifiedList()
                .stream()
                .map(c -> socketIOServer.getClient(UUID.fromString(c)))
                .collect(Collectors.toList());

        String json = Casts.writeValueAsString(message.getMessage());

        Assert.hasText(json, "推送消息不能为空");

        clientOperations.forEach(c -> AbstractMessageSender.sendEventMessage(c, socketMessage.getEvent(), json));

        log.info(
                "发送消息 [事件类型: {}, 数据: {}}] 到设备 [{}] 成功",
                message.getEvent(),
                json,
                message.getDeviceIdentifiedList()
        );
    }

    @Override
    public boolean isSupport(SocketMessage<?> socketMessage) {
        return MultipleUnicastMessage.class.isAssignableFrom(socketMessage.getClass());
    }
}
