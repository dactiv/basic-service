package com.github.dactiv.basic.socket.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.dao.RoomDao;
import com.github.dactiv.basic.socket.server.domain.body.response.RoomResponseBody;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.enumerate.RoomParticipantRoleEnum;
import com.github.dactiv.basic.socket.server.receiver.CreateRoomMessageReceiver;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * tb_room 的业务逻辑
 *
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @see RoomEntity
 *
 * @author maurice.chen
 *
 * @since 2021-12-10 11:17:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoomService extends BasicService<RoomDao, RoomEntity> {

    private final RoomParticipantService roomParticipantService;

    private final AmqpTemplate amqpTemplate;

    public RoomService(RoomParticipantService roomParticipantService, AmqpTemplate amqpTemplate) {
        this.roomParticipantService = roomParticipantService;
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 创建房间实体
     *
     * @param room 房间信息
     * @param userIds 用户 id 集合
     * @param ownerId 拥有者 id
     */
    public RoomResponseBody create(RoomEntity room, List<Integer> userIds, Integer ownerId) {

        save(room);

        List<RoomParticipantEntity> roomParticipants = userIds
                .stream()
                .map(id -> RoomParticipantEntity.of(id, RoomParticipantRoleEnum.Participant, room.getId()))
                .collect(Collectors.toList());

        Optional<RoomParticipantEntity> optional = roomParticipants
                .stream()
                .filter(p -> p.getUserId().equals(ownerId))
                .findFirst();

        if (optional.isPresent()) {
            optional.get().setRole(RoomParticipantRoleEnum.Owner);
        } else {
            roomParticipants.add(RoomParticipantEntity.of(ownerId, RoomParticipantRoleEnum.Owner, room.getId()));
        }

        roomParticipantService.save(roomParticipants);

        RoomResponseBody body = Casts.of(room, RoomResponseBody.class);
        body.setParticipantList(roomParticipants);

        amqpTemplate.convertAndSend(
                Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                CreateRoomMessageReceiver.DEFAULT_QUEUE_NAME,
                room.getId()
        );

        return body;
    }

    /**
     * 根据用户 id 获取房间集合
     *
     * @param userId 用户 id
     *
     * @return 房间集合
     */
    public List<RoomEntity> findByUserId(Integer userId) {
        return findByUserId(userId, null);
    }

    /**
     * 根据用户 id 获取房间集合
     *
     * @param userId  用户 id
     * @param wrapper 查询条件
     *
     * @return 房间集合
     */
    public List<RoomEntity> findByUserId(Integer userId, LambdaQueryWrapper<RoomEntity> wrapper) {
        return getBaseMapper().findByUserId(userId, wrapper);
    }

    /**
     * 根据用户 id 获取房间响应实体集合
     *
     * @param userId 用户 id
     *
     * @return 房间响应实体集合
     */
    public List<RoomResponseBody> findResponseBodiesByUserid(Integer userId) {

        List<RoomEntity> rooms = findByUserId(userId);

        return rooms
                .stream()
                .map(r -> Casts.of(r, RoomResponseBody.class))
                .peek(r -> r.setParticipantList(roomParticipantService.findByRoomId(r.getId())))
                .collect(Collectors.toList());

    }

    /**
     * 退出/解散房间
     *
     * @param userId 当前用户 id
     * @param id 房间 id
     *
     * @return true 解散房间，false 退出房间
     */
    public boolean exitRoom(Integer userId, Integer id) {

        RoomParticipantEntity entity = roomParticipantService.lambdaQuery()
                .select(RoomParticipantEntity::getId, RoomParticipantEntity::getRole)
                .eq(RoomParticipantEntity::getUserId, userId)
                .eq(RoomParticipantEntity::getRoomId, id)
                .one();

        boolean result = RoomParticipantRoleEnum.Owner.equals(entity.getRole());

        if (result) {
            roomParticipantService.lambdaUpdate().eq(RoomParticipantEntity::getRoomId, id).remove();
        }

        deleteById(id);

        return result;
    }
}
