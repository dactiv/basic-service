package com.github.dactiv.basic.socket.server.receiver;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.service.RoomParticipantService;
import com.github.dactiv.basic.socket.server.service.RoomService;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
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
 * 创建房间 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class CreateRoomMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "create.room.message";

    private final SocketServerManager socketServerManager;

    private final SocketClientTemplate socketClientTemplate;

    private final RoomParticipantService roomParticipantService;

    public CreateRoomMessageReceiver(SocketServerManager socketServerManager,
                                     SocketClientTemplate socketClientTemplate,
                                     RoomParticipantService roomParticipantService) {
        this.socketServerManager = socketServerManager;
        this.socketClientTemplate = socketClientTemplate;
        this.roomParticipantService = roomParticipantService;
    }

    @SocketMessage
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload Integer id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        Wrapper<RoomParticipantEntity> wrapper = Wrappers
                .<RoomParticipantEntity>lambdaQuery()
                .select(RoomParticipantEntity::getUserId)
                .eq(RoomParticipantEntity::getRoomId, id);

        List<Integer> userIds = roomParticipantService.findObjects(wrapper, Integer.class);

        List<String> deviceIdentifies = userIds
                .stream()
                .map(socketServerManager::getSocketUserDetails)
                .filter(Objects::nonNull)
                .map(MobileUserDetails::getDeviceIdentified)
                .collect(Collectors.toList());

        socketClientTemplate.joinRoom(deviceIdentifies, List.of(id.toString()));

        SocketResultHolder.get().addBroadcastSocketMessage(id.toString(), RoomEntity.ROOM_CREATE_EVENT_NAME, userIds);

        channel.basicAck(tag, false);
    }
}
