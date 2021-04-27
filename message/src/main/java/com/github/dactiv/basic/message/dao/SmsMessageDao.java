package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.dao.entity.SmsMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 短信消息访问
 *
 * @author maurice
 * @since 2020-04-06 09:15:36
 */
@Mapper
@Repository
public interface SmsMessageDao extends BaseMapper<SmsMessage> {

}
