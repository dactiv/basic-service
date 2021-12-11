package com.github.dactiv.basic.message.service;

import com.github.dactiv.basic.message.dao.EmailMessageDao;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.basic.message.enumerate.AttachmentTypeEnum;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 *
 * tb_email_message 的业务逻辑
 *
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @see EmailMessageEntity
 *
 * @author maurice.chen
 *
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EmailMessageService extends BasicService<EmailMessageDao, EmailMessageEntity> {

    private final AttachmentService attachmentService;

    public EmailMessageService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @Override
    public EmailMessageEntity get(Serializable id) {
        EmailMessageEntity result = super.get(id);

        if (YesOrNo.Yes.equals(result.getHasAttachment())) {
            List<AttachmentEntity> attachmentList = attachmentService
                    .lambdaQuery()
                    .eq(AttachmentEntity::getMessageId, result.getId())
                    .eq(AttachmentEntity::getType, AttachmentTypeEnum.Email.getValue())
                    .list();
            result.setAttachmentList(attachmentList);
        }

        return result;
    }
}
