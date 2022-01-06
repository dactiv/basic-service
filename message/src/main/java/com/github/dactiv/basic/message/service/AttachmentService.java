package com.github.dactiv.basic.message.service;

import com.github.dactiv.basic.message.dao.AttachmentDao;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_attachment 的业务逻辑
 *
 * <p>Table: tb_attachment - 消息附件</p>
 *
 * @author maurice.chen
 * @see AttachmentEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachmentService extends BasicService<AttachmentDao, AttachmentEntity> {

}
