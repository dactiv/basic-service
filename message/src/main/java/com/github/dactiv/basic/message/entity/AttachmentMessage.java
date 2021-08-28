package com.github.dactiv.basic.message.entity;

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
    List<Attachment> getAttachmentList();
}
