package com.github.dactiv.basic.socket.server.service.message.support;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.client.entity.SocketMessage;
import com.github.dactiv.basic.socket.client.entity.UnicastMessage;
import com.github.dactiv.basic.socket.server.service.message.AbstractMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 单播消息发送者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class UnicastMessageSender extends AbstractMessageSender<UnicastMessage<?>> {

    @Override
    public boolean isSupport(SocketMessage<?> socketMessage) {
        return UnicastMessage.class.isAssignableFrom(socketMessage.getClass());
    }

    @Override
    protected ClientOperations getClientOperations(UnicastMessage<?> message, SocketIOServer socketIOServer) {
        String deviceIdentified = message.getDeviceIdentified();

        if (StringUtils.isNotBlank(deviceIdentified)) {
            return socketIOServer.getClient(UUID.fromString(deviceIdentified));
        }

        return null;
    }

    @Override
    protected void afterSetting(ClientOperations clientOperations, UnicastMessage<?> message, String json) {
        log.info(
                "发送消息 [事件类型: {}, 数据: {}}] 到设备 [{}] 成功",
                message.getEvent(),
                json,
                message.getDeviceIdentified()
        );
    }
}
