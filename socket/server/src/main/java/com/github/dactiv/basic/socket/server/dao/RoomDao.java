package com.github.dactiv.basic.socket.server.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.socket.server.enitty.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_room 的数据访问
 *
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @see Room
 *
 * @author maurice
 *
 * @since 2021-10-08 10:36:59
 */
@Mapper
@Repository
public interface RoomDao extends BaseMapper<Room> {

    @Select("<script>" +
            "SELECT " +
            "   tb_room.id, " +
            "   tb_room.name, " +
            "   tb_room.creation_time, " +
            "   tb_room.last_message_time, " +
            "   tb_room.type, " +
            "   tb_room.remark " +
            "FROM " +
            "   tb_room " +
            "INNER JOIN " +
            "   tb_room_participant on tb_room_participant.room_id = tb_room.id " +
            "WHERE " +
            "   user_id = #{userId} " +
            "<if test='ew != null'>" +
            "   AND ${ew.expression.sqlSegment}" +
            "</if>" +
            "</script>")
    List<Room> findByUserId(@Param("userId") Integer userId, @Param("ew") Wrapper<Room> wrapper);

}
