package com.github.dactiv.basic.socket.server.service.message.support;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.namespace.Namespace;
import com.github.dactiv.basic.socket.client.entity.BroadcastMessage;
import com.github.dactiv.basic.socket.client.entity.SocketMessage;
import com.github.dactiv.basic.socket.server.service.message.AbstractMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 广播消息发送者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class BroadcastMessageSender extends AbstractMessageSender<BroadcastMessage<?>> {

    @Override
    protected ClientOperations getClientOperations(BroadcastMessage<?> message, SocketIOServer socketIOServer) {

        BroadcastOperations broadcastOperations = socketIOServer.getRoomOperations(Namespace.DEFAULT_NAME);

        String room = message.getRoom();

        if (StringUtils.isNotEmpty(room)) {

            broadcastOperations = socketIOServer.getRoomOperations(room);

        }

        return broadcastOperations;
    }

    @Override
    protected void afterSetting(ClientOperations clientOperations, BroadcastMessage<?> message, String json) {

        log.info(
                "发送广播消息 [类型: {}, 数据: {}] 到 [{}] 频道成功",
                message.getEvent(),
                json,
                StringUtils.isNotEmpty(message.getRoom()) ? message.getRoom() : "全网");
    }

    @Override
    public boolean isSupport(SocketMessage<?> socketMessage) {
        return BroadcastMessage.class.isAssignableFrom(socketMessage.getClass());
    }
}
