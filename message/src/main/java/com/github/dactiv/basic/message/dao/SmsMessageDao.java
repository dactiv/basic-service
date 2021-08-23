package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.entity.SmsMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_sms_message 短信消息数据访问
 *
 * <p>Table: tb_sms_message - 站内信消息</p>
 *
 * @see SiteMessage
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface SmsMessageDao extends BaseMapper<SmsMessage> {

}
