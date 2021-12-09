package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.basic.message.domain.entity.SmsMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_sms_message 短信消息数据访问
 *
 * <p>Table: tb_sms_message - 站内信消息</p>
 *
 * @author maurice
 * @see SiteMessageEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface SmsMessageDao extends BaseMapper<SmsMessageEntity> {

}
