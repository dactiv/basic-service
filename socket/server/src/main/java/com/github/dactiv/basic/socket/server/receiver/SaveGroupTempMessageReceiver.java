package com.github.dactiv.basic.socket.server.receiver;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.domain.BroadcastMessage;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.service.RoomParticipantService;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 存储群聊临时消息的 MQ 接收者
 */
@Component
public class SaveGroupTempMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "send.group.message";

    private final RoomParticipantService roomParticipantService;

    private final SocketServerManager socketServerManager;

    public SaveGroupTempMessageReceiver(RoomParticipantService roomParticipantService,
                                        SocketServerManager socketServerManager) {
        this.roomParticipantService = roomParticipantService;
        this.socketServerManager = socketServerManager;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    public void onMessage(@Payload BroadcastMessage<?> message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        Wrapper<RoomParticipantEntity> wrapper = Wrappers
                .<RoomParticipantEntity>lambdaQuery()
                .select(RoomParticipantEntity::getUserId)
                .eq(RoomParticipantEntity::getRoomId, message.getRoom());

        List<Integer> userIds = roomParticipantService.findObjects(wrapper, Integer.class);

        List<Integer> offLine = userIds
                .stream()
                .filter(u -> Objects.isNull(socketServerManager.getSocketUserDetails(u)))
                .collect(Collectors.toList());

        for (Integer id : offLine) {
            socketServerManager.saveTempMessage(id, message.getType(), message.getMessage());
        }

        channel.basicAck(tag, false);

    }

}
