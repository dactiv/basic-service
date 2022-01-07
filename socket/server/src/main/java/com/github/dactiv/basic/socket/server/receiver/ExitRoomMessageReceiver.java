package com.github.dactiv.basic.socket.server.receiver;

import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.service.RoomParticipantService;
import com.github.dactiv.basic.socket.server.service.RoomService;
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
import java.util.stream.Collectors;

/**
 * 解散房间 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class ExitRoomMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "exit.room.message";

    private final RoomService roomService;

    private final RoomParticipantService roomParticipantService;

    public ExitRoomMessageReceiver(RoomService roomService, RoomParticipantService roomParticipantService) {
        this.roomService = roomService;
        this.roomParticipantService = roomParticipantService;
    }

    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload Integer id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        List<RoomParticipantEntity> participants = roomParticipantService
                .lambdaQuery()
                .select(RoomParticipantEntity::getId, RoomParticipantEntity::getUserId)
                .eq(RoomParticipantEntity::getRoomId, id)
                .list();

        List<Integer> participantIds = participants
                .stream()
                .map(RoomParticipantEntity::getId)
                .collect(Collectors.toList());

        roomParticipantService
                .lambdaUpdate()
                .in(RoomParticipantEntity::getId, participantIds)
                .remove();

        roomService.deleteById(id);

        SocketResultHolder.get().addBroadcastSocketMessage(id.toString(), RoomEntity.ROOM_DELETE_EVENT_NAME, id);

        channel.basicAck(tag, false);
    }
}
