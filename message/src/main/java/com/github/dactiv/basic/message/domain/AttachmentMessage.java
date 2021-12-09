package com.github.dactiv.basic.message.domain;

import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;

import java.util.List;

/**
 * 带附件的消息
 *
 * @author maurice.chen
 */
public interface AttachmentMessage {

    /**
     * 获取附件信息集合
     *
     * @return 附件信息集合
     */
    List<AttachmentEntity> getAttachmentList();
}
