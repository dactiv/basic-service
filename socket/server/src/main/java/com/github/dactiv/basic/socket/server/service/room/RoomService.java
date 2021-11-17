package com.github.dactiv.basic.socket.server.service.room;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.controller.room.RoomResponseBody;
import com.github.dactiv.basic.socket.server.dao.RoomDao;
import com.github.dactiv.basic.socket.server.dao.RoomParticipantDao;
import com.github.dactiv.basic.socket.server.enitty.Room;
import com.github.dactiv.basic.socket.server.enitty.RoomParticipant;
import com.github.dactiv.basic.socket.server.enumerate.RoomParticipantRole;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.web.query.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 房间业务逻辑服务
 *
 * @author maurice.chen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoomService {
    /**
     * 聊天信息事件名称
     */
    public static final String ROOM_CREATE_EVENT_NAME = "room_create";

    @Autowired
    private RoomParticipantDao roomParticipantDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private SocketServerManager socketServerManager;

    /**
     * 创建房间实体
     *
     * @param room 房间信息
     * @param userIds 用户 id 集合
     * @param ownerId 拥有者 id
     */
    @SocketMessage
    public void createRoom(Room room, List<Integer> userIds, Integer ownerId) {

        saveRoom(room);

        List<RoomParticipant> roomParticipants = userIds
                .stream()
                .map(id -> RoomParticipant.of(id, RoomParticipantRole.Participant.getValue(), room.getId()))
                .collect(Collectors.toList());

        Optional<RoomParticipant> optional = roomParticipants
                .stream()
                .filter(p -> p.getUserId().equals(ownerId))
                .findFirst();

        if (optional.isPresent()) {
            optional.get().setRole(RoomParticipantRole.Owner.getValue());
        } else {
            roomParticipants.add(RoomParticipant.of(ownerId, RoomParticipantRole.Owner.getValue(), room.getId()));
        }

        roomParticipants
                .stream()
                .peek(this::saveRoomParticipant)
                .map(r -> socketServerManager.getSocketUserDetails(r.getUserId()))
                .filter(Objects::nonNull)
                .forEach(s -> socketServerManager.joinRoom(s, room.getId().toString()));

        RoomResponseBody body = Casts.of(room, RoomResponseBody.class);
        body.setParticipantList(roomParticipants);

        SocketResultHolder.get().addBroadcastSocketMessage(room.getName(), ROOM_CREATE_EVENT_NAME, body);
    }

    // ------------------------- 房间业务逻辑 ------------------------- //

    /**
     * 保存 table : tb_room 实体
     *
     * @param room 实体
     *
     * @see Room
     */
    public void saveRoom(Room room) {
        if (Objects.isNull(room.getId())) {
            insertRoom(room);
        } else {
            updateRoom(room);
        }
    }

    /**
     * 新增 table : tb_room 实体
     *
     * @param room 实体
     *
     * @see Room
     */
    public void insertRoom(Room room) {
        roomDao.insert(room);
    }

    /**
     * 更新 table : tb_room 实体
     *
     * @param room 实体
     *
     * @see Room
     */
    public void updateRoom(Room room) {
        roomDao.updateById(room);
    }

    /**
     * 删除 table : tb_room 实体
     *
     * @param id 主键 id
     *
     * @see Room
     */
    public void deleteRoom(Integer id) {
        roomDao.deleteById(id);
    }

    /**
     * 删除 table : tb_room 实体
     *
     * @param ids 主键 id 集合
     *
     * @see Room
     */
    public void deleteRoom(List<Integer> ids) {
        ids.forEach(this::deleteRoom);
    }

    /**
     * 获取 table : tb_room 实体
     *
     * @param id 主键 id
     *
     * @return tb_room 实体
     *
     * @see Room
     */
    public Room getRoom(Integer id) {
        return roomDao.selectById(id);
    }

    /**
     * 获取 table : tb_room 实体
     *
     * @param wrapper 过滤条件
     *
     * @return tb_room 实体
     *
     * @see Room
     */
    public Room findOneRoom(Wrapper<Room> wrapper) {
        return roomDao.selectOne(wrapper);
    }

    /**
     * 根据过滤条件查找 table : tb_room 实体
     *
     * @param wrapper 过滤条件
     *
     * @return tb_room 实体集合
     *
     * @see Room
     */
    public List<Room> findRoomList(Wrapper<Room> wrapper) {
        return roomDao.selectList(wrapper);
    }

    /**
     * 查找 table : tb_room 实体分页数据
     *
     * @param pageable 分页请求
     * @param wrapper  过滤条件
     *
     * @return 分页实体
     *
     * @see Room
     */
    public Page<Room> findRoomPage(PageRequest pageable, Wrapper<Room> wrapper) {

        IPage<Room> result = roomDao.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageable),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 根据用户 id 获取房间集合
     *
     * @param userId 用户 id
     *
     * @return 房间集合
     */
    public List<Room> findRoomList(Integer userId) {
        return findRoomList(userId, null);
    }

    /**
     * 根据用户 id 获取房间集合
     *
     * @param userId  用户 id
     * @param wrapper 查询条件
     *
     * @return 房间集合
     */
    public List<Room> findRoomList(Integer userId, LambdaQueryWrapper<Room> wrapper) {
        return roomDao.findByUserId(userId, wrapper);
    }

    /**
     * 根据用户 id 获取房间响应实体集合
     *
     * @param userId 用户 id
     *
     * @return 房间响应实体集合
     */
    public List<RoomResponseBody> findRoomResponseBodyList(Integer userId) {

        List<Room> rooms = findRoomList(userId);

        return rooms
                .stream()
                .map(r -> Casts.of(r, RoomResponseBody.class))
                .peek(r -> r.setParticipantList(this.findRoomParticipantListByRoomId(r.getId())))
                .collect(Collectors.toList());

    }

    // ------------------------- 房间参与者业务逻辑 ------------------------- //

    /**
     * 保存 table : tb_room_participant 实体
     *
     * @param roomParticipant 实体
     *
     * @see RoomParticipant
     */
    public void saveRoomParticipant(RoomParticipant roomParticipant) {
        if (Objects.isNull(roomParticipant.getId())) {
            insertRoomParticipant(roomParticipant);
        } else {
            updateRoomParticipant(roomParticipant);
        }
    }

    /**
     * 新增 table : tb_room_participant 实体
     *
     * @param roomParticipant 实体
     *
     * @see RoomParticipant
     */
    public void insertRoomParticipant(RoomParticipant roomParticipant) {
        roomParticipantDao.insert(roomParticipant);
    }

    /**
     * 更新 table : tb_room_participant 实体
     *
     * @param roomParticipant 实体
     *
     * @see RoomParticipant
     */
    public void updateRoomParticipant(RoomParticipant roomParticipant) {
        roomParticipantDao.updateById(roomParticipant);
    }

    /**
     * 删除 table : tb_room_participant 实体
     *
     * @param id 主键 id
     *
     * @see RoomParticipant
     */
    public void deleteRoomParticipant(Integer id) {
        roomParticipantDao.deleteById(id);
    }

    /**
     * 删除 table : tb_room_participant 实体
     *
     * @param ids 主键 id 集合
     *
     * @see RoomParticipant
     */
    public void deleteRoomParticipant(List<Integer> ids) {
        ids.forEach(this::deleteRoomParticipant);
    }

    /**
     * 获取 table : tb_room_participant 实体
     *
     * @param id 主键 id
     *
     * @return tb_room_participant 实体
     *
     * @see RoomParticipant
     */
    public RoomParticipant getRoomParticipant(Integer id) {
        return roomParticipantDao.selectById(id);
    }

    /**
     * 获取 table : tb_room_participant 实体
     *
     * @param wrapper 过滤条件
     *
     * @return tb_room_participant 实体
     *
     * @see RoomParticipant
     */
    public RoomParticipant findOneRoomParticipant(Wrapper<RoomParticipant> wrapper) {
        return roomParticipantDao.selectOne(wrapper);
    }

    /**
     * 根据过滤条件查找 table : tb_room_participant 实体
     *
     * @param wrapper 过滤条件
     *
     * @return tb_room_participant 实体集合
     *
     * @see RoomParticipant
     */
    public List<RoomParticipant> findRoomParticipantList(Wrapper<RoomParticipant> wrapper) {
        return roomParticipantDao.selectList(wrapper);
    }

    /**
     * 根据房间 id 查找 table : tb_room_participant 实体
     *
     * @param roomId 过滤条件
     *
     * @return tb_room_participant 实体集合
     *
     * @see RoomParticipant
     */
    public List<RoomParticipant> findRoomParticipantListByRoomId(Integer roomId) {
        return findRoomParticipantList(Wrappers.<RoomParticipant>lambdaQuery().eq(RoomParticipant::getRoomId, roomId));
    }

    /**
     * 查找 table : tb_room_participant 实体分页数据
     *
     * @param pageable 分页请求
     * @param wrapper  过滤条件
     *
     * @return 分页实体
     *
     * @see RoomParticipant
     */
    public Page<RoomParticipant> findRoomParticipantPage(PageRequest pageable, Wrapper<RoomParticipant> wrapper) {

        IPage<RoomParticipant> result = roomParticipantDao.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageable),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }
}
