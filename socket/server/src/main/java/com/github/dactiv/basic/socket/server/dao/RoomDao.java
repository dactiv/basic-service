package com.github.dactiv.basic.socket.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_room 的数据访问
 *
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @author maurice
 * @see RoomEntity
 * @since 2021-10-08 10:36:59
 */
@Mapper
@Repository
public interface RoomDao extends BaseMapper<RoomEntity> {

}
