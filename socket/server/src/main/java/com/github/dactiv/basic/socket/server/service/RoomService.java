package com.github.dactiv.basic.socket.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.commons.ErrorCodeConstants;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.dao.RoomDao;
import com.github.dactiv.basic.socket.server.domain.dto.RoomDto;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.enumerate.RoomParticipantRoleEnum;
import com.github.dactiv.basic.socket.server.receiver.CreateRoomMessageReceiver;
import com.github.dactiv.basic.socket.server.receiver.ExitRoomMessageReceiver;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * tb_room 的业务逻辑
 *
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @author maurice.chen
 * @see RoomEntity
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
     * @param room    房间信息
     * @param userIds 用户 id 集合
     * @param ownerId 拥有者 id
     */
    @Concurrent(value = "socket-server:create-room:[#ownerId]")
    public void create(RoomEntity room, List<Integer> userIds, Integer ownerId) {

        save(room);

        List<RoomParticipantEntity> roomParticipants = userIds
                .stream()
                .map(id -> RoomParticipantEntity.of(id, RoomParticipantRoleEnum.PARTICIPANT, room.getId()))
                .collect(Collectors.toList());

        Optional<RoomParticipantEntity> optional = roomParticipants
                .stream()
                .filter(p -> p.getUserId().equals(ownerId))
                .findFirst();

        if (optional.isPresent()) {
            optional.get().setRole(RoomParticipantRoleEnum.OWNER);
        } else {
            roomParticipants.add(RoomParticipantEntity.of(ownerId, RoomParticipantRoleEnum.OWNER, room.getId()));
        }

        roomParticipantService.save(roomParticipants);

        RoomDto dto = Casts.of(room, RoomDto.class);
        dto.setParticipantList(roomParticipants);

        amqpTemplate.convertAndSend(
                SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                CreateRoomMessageReceiver.DEFAULT_QUEUE_NAME,
                dto
        );
    }

    /**
     * 根据用户 id 获取房间集合
     *
     * @param userId 用户 id
     *
     * @return 房间集合
     */
    public List<Integer> findRoomIdsByUserId(Integer userId) {
        Wrapper<RoomParticipantEntity> wrapper = Wrappers
                .<RoomParticipantEntity>lambdaQuery()
                .select(RoomParticipantEntity::getRoomId)
                .eq(RoomParticipantEntity::getUserId, userId);
        return roomParticipantService.findObjects(wrapper, Integer.class);
    }

    /**
     * 根据用户 id 获取房间响应实体集合
     *
     * @param userId 用户 id
     *
     * @return 房间响应实体集合
     */
    public List<RoomDto> findByUserId(Integer userId) {

        List<Integer> ids = findRoomIdsByUserId(userId);

        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedList<>();
        }

        return getRomDto(ids);

    }

    /**
     * 获取房间 dto
     *
     * @param ids 房间主键 id 集合
     *
     * @return 房间 dto 集合
     */
    public List<RoomDto> getRomDto(List<Integer> ids) {
        List<RoomEntity> rooms = lambdaQuery()
                .in(RoomEntity::getId, ids)
                .list();

        List<RoomParticipantEntity> participantList = roomParticipantService
                .lambdaQuery()
                .in(RoomParticipantEntity::getRoomId, ids)
                .list();

        return rooms
                .stream()
                .map(r -> Casts.of(r, RoomDto.class))
                .peek(r -> r.setParticipantList(
                        participantList
                                .stream()
                                .filter(p -> p.getRoomId().equals(r.getId()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 退出/解散房间
     *
     * @param userId 当前用户 id
     * @param id     房间 id
     *
     * @return true 解散房间，false 退出房间
     */
    @Concurrent(value = "socket-server:exit-room:[#userId]")
    public boolean exitRoom(Integer userId, Integer id) {

        RoomParticipantEntity entity = roomParticipantService.lambdaQuery()
                .select(RoomParticipantEntity::getRole)
                .eq(RoomParticipantEntity::getUserId, userId)
                .eq(RoomParticipantEntity::getRoomId, id)
                .one();

        ErrorCodeException.isTrue(
                Objects.nonNull(entity),
                "该群聊不存在或者您已经不在此群聊内。",
                ErrorCodeConstants.NOT_CONTENT
        );

        boolean result = RoomParticipantRoleEnum.OWNER.equals(entity.getRole());
        // 如果为群主，直接解散房间。
        if (result) {
            RoomEntity roomEntity = new RoomEntity();
            roomEntity.setId(id);
            roomEntity.setStatus(DisabledOrEnabled.Disabled);

            updateById(roomEntity);

            amqpTemplate.convertAndSend(
                    SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                    ExitRoomMessageReceiver.DEFAULT_QUEUE_NAME,
                    id
            );
        } else {
            roomParticipantService.deleteById(entity.getId());
        }

        return result;
    }

    /**
     * 修改房间名称
     *
     * @param userId 修改用户 id
     * @param name   新的房间名称
     * @param id     房间 id
     *
     * @return true 修改成功, false 不做任何修改
     */
    public boolean rename(Integer userId, String name, Integer id) {
        RoomParticipantEntity entity = roomParticipantService.lambdaQuery()
                .eq(RoomParticipantEntity::getUserId, userId)
                .eq(RoomParticipantEntity::getRoomId, id)
                .one();

        ErrorCodeException.isTrue(
                Objects.nonNull(entity),
                "您不在此群聊内，无法修改名称。",
                ErrorCodeConstants.NOT_CONTENT
        );

        RoomEntity room = get(entity.getRoomId());

        if (room.getName().equals(name)) {
            return false;
        }

        RoomEntity roomEntity = room.ofIdData();
        roomEntity.setName(name);
        updateById(roomEntity);

        return true;
    }
}
