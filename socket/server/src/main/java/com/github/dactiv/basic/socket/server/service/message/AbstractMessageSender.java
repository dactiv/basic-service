package com.github.dactiv.basic.socket.server.service.message;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.client.domain.SocketMessage;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 抽象的消息附送实现
 *
 * @param <T> 继承与 {@link SocketMessage} 的子类
 */
@Slf4j
public abstract class AbstractMessageSender<T extends SocketMessage<?>> implements MessageSender {

    @Override
    public void sendMessage(SocketMessage<?> socketMessage, SocketIOServer socketIOServer) {

        Assert.hasText(socketMessage.getEvent(), "推送事件不能为空");

        T message = Casts.cast(socketMessage);

        ClientOperations clientOperations = getClientOperations(message, socketIOServer);

        if (Objects.isNull(clientOperations)) {
            log.warn("找不到 [" + socketIOServer + "] 的客户端操作");
            return;
        }

        String json = Casts.writeValueAsString(message.getMessage());

        Assert.hasText(json, "推送消息不能为空");

        sendEventMessage(clientOperations, message.getEvent(), json);

        afterSetting(clientOperations, message, json);
    }

    /**
     * 后置设置
     *
     * @param clientOperations 客户端操作
     * @param message          继承与 {@link SocketMessage} 的子类
     * @param json             {@link SocketMessage#getMessage()} 的 json 字符串
     */
    protected void afterSetting(ClientOperations clientOperations, T message, String json) {

    }

    /**
     * 获取客户端操作类
     *
     * @param message 继承与 {@link SocketMessage} 的子类
     *
     * @return 客户端操作类
     */
    protected abstract ClientOperations getClientOperations(T message, SocketIOServer socketIOServer);

    /**
     * 发送事件消息
     *
     * @param client  客户端
     * @param message 消息
     */
    public static void sendEventMessage(ClientOperations client, String event, String message) {
        client.sendEvent(event, message);

        if (SocketServerManager.SERVER_DISCONNECT_EVENT_NAME.equals(event)) {
            client.disconnect();
        }
    }
}
