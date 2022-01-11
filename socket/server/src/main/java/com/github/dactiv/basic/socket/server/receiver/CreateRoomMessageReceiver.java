package com.github.dactiv.basic.socket.server.receiver;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.dto.RoomDto;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.service.RoomParticipantService;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
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
import java.util.Map;
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

    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload RoomDto dto,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        Wrapper<RoomParticipantEntity> wrapper = Wrappers
                .<RoomParticipantEntity>lambdaQuery()
                .select(RoomParticipantEntity::getUserId)
                .eq(RoomParticipantEntity::getRoomId, dto.getId());

        List<Integer> userIds = roomParticipantService.findObjects(wrapper, Integer.class);

        List<SocketUserDetails> details = userIds
                .stream()
                .map(socketServerManager::getSocketUserDetails)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = socketClientTemplate.exchangeRoomOperation(
                details,
                List.of(dto.getId().toString()),
                SocketClientTemplate.JOIN_ROOM_TYPE
        );

        boolean success = result
                .stream()
                .map(m -> m.get(RestResult.DEFAULT_STATUS_NAME))
                .allMatch(v -> v.toString().equals(RestResult.SUCCESS_EXECUTE_CODE));

        if (!success) {
            throw new SystemException("调用加入房间出现错误, 响应结果为:" + result);
        }

        SocketResultHolder
                .get()
                .addBroadcastSocketMessage(
                        dto.getId().toString(),
                        RoomEntity.ROOM_CREATE_EVENT_NAME,
                        dto
                );

        channel.basicAck(tag, false);
    }
}
