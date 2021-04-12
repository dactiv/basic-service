package com.fuyu.basic.message.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.message.dao.entity.EmailMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 邮件消息访问
 *
 * @author maurice
 * @since 2020-04-06 09:15:36
 */
@Mapper
@Repository
public interface EmailMessageDao extends BasicCurdDao<EmailMessage, Integer> {

}
