package com.github.dactiv.basic.message.service;

import com.github.dactiv.basic.message.dao.SmsMessageDao;
import com.github.dactiv.basic.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_sms_message 的业务逻辑
 *
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice.chen
 * @see SmsMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SmsMessageService extends BasicService<SmsMessageDao, SmsMessageEntity> {

}
