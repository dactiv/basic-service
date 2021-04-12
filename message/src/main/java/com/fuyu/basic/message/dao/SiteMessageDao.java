package com.fuyu.basic.message.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.message.dao.entity.SiteMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
public interface SiteMessageDao extends BasicCurdDao<SiteMessage, Integer> {

    /**
     * 技术纬度数量
     *
     * @param userId 用户 id
     * @return 按类型分组的计数集合
     */
    List<Map<String, Object>> countUnreadQuantity(@Param("userId") Integer userId);
}
