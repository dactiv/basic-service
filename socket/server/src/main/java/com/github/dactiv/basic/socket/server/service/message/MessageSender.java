package com.github.dactiv.basic.socket.server.service.message;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.client.entity.SocketMessage;

/**
 * 消息发送者
 *
 * @author maurice.chen
 */
public interface MessageSender {

    /**
     * 发送消息
     *
     * @param socketMessage socket 消息
     * @param socketIOServer socket 服务
     */
    void sendMessage(SocketMessage<?> socketMessage, SocketIOServer socketIOServer);

    /**
     * 是否支持此消息类型
     *
     * @param socketMessage socket 消息
     *
     * @return true 是，否则 false
     */
    boolean isSupport(SocketMessage<?> socketMessage);

}
