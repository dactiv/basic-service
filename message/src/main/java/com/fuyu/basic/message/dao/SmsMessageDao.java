package com.fuyu.basic.message.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.message.dao.entity.SmsMessage;
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
public interface SmsMessageDao extends BasicCurdDao<SmsMessage, Integer> {

}
