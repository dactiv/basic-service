package com.github.dactiv.basic.socket.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_room_participant 的数据访问
 *
 * <p>Table: tb_room_participant - 房间参与者，用于说明某个房间里存在些什么人</p>
 *
 * @see RoomParticipant
 *
 * @author maurice
 *
 * @since 2021-10-08 10:36:59
 */
@Mapper
@Repository
public interface RoomParticipantDao extends BaseMapper<RoomParticipant> {

}
