package com.github.dactiv.basic.socket.server.service;

import com.github.dactiv.basic.socket.server.dao.RoomParticipantDao;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * tb_room_participant 的业务逻辑
 *
 * <p>Table: tb_room_participant - 房间参与者，用于说明某个房间里存在些什么人</p>
 *
 * @see RoomParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2021-12-10 11:17:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoomParticipantService extends BasicService<RoomParticipantDao, RoomParticipantEntity> {

    /**
     * 根据房间 id 查找 table : tb_room_participant 实体
     *
     * @param roomId 过滤条件
     *
     * @return tb_room_participant 实体集合
     *
     * @see RoomParticipantEntity
     */
    public List<RoomParticipantEntity> findByRoomId(Integer roomId) {
        return lambdaQuery().eq(RoomParticipantEntity::getRoomId, roomId).list();
    }
}
