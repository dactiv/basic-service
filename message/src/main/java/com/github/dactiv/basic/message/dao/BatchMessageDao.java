package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.entity.BatchMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_batch_message 批量消息数据访问
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @see BatchMessage
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface BatchMessageDao extends BaseMapper<BatchMessage> {

}
