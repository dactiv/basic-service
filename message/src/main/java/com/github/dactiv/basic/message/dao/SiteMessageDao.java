package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.entity.SiteMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 站内信消息访问
 *
 * @author maurice
 * @since 2020-04-06 09:15:36
 */
@Mapper
@Repository
public interface SiteMessageDao extends BaseMapper<SiteMessage> {

    /**
     * 技术纬度数量
     *
     * @param userId 用户 id
     * @return 按类型分组的计数集合
     */
    @Select(
            "SELECT " +
            "    type 'type', " +
            "    COUNT(id) 'quantity' " +
            "FROM " +
            "    tb_site_message " +
            "WHERE " +
            "    to_user_id = #{userId} " +
            "GROUP BY " +
            "    type"
    )
    List<Map<String, Object>> countUnreadQuantity(@Param("userId") Integer userId);
}
