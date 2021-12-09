package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.domain.entity.EmailMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * tb_email_message 邮件消息数据访问
 *
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @see EmailMessageEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface EmailMessageDao extends BaseMapper<EmailMessageEntity> {

}
