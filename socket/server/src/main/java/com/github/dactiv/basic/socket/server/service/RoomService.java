package com.github.dactiv.basic.socket.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.dactiv.basic.socket.server.dao.RoomDao;
import com.github.dactiv.basic.socket.server.domain.body.response.RoomResponseBody;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.enumerate.RoomParticipantRoleEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
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

    private final SocketServerManager socketServerManager;

    public RoomService(RoomParticipantService roomParticipantService,
                       SocketServerManager socketServerManager) {
        this.roomParticipantService = roomParticipantService;
        this.socketServerManager = socketServerManager;
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
                .map(id -> RoomParticipantEntity.of(id, RoomParticipantRoleEnum.Participant.getValue(), room.getId()))
                .collect(Collectors.toList());

        Optional<RoomParticipantEntity> optional = roomParticipants
                .stream()
                .filter(p -> p.getUserId().equals(ownerId))
                .findFirst();

        if (optional.isPresent()) {
            optional.get().setRole(RoomParticipantRoleEnum.Owner.getValue());
        } else {
            roomParticipants.add(RoomParticipantEntity.of(ownerId, RoomParticipantRoleEnum.Owner.getValue(), room.getId()));
        }

        roomParticipants
                .stream()
                .peek(roomParticipantService::save)
                .map(r -> socketServerManager.getSocketUserDetails(r.getUserId()))
                .filter(Objects::nonNull)
                .forEach(s -> socketServerManager.joinRoom(s, room.getId().toString()));

        RoomResponseBody body = Casts.of(room, RoomResponseBody.class);
        body.setParticipantList(roomParticipants);

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
}
