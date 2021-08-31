package com.github.dactiv.basic.message.service.support.body;

import com.github.dactiv.basic.message.entity.Attachment;
import com.github.dactiv.basic.message.entity.AttachmentMessage;
import com.github.dactiv.basic.message.entity.TitleMessage;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 站内信消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SiteMessageBody extends TitleMessage implements AttachmentMessage {

    private static final long serialVersionUID = 4341146261560926962L;

    /**
     * 接收方用户 id 集合
     */
    private List<Integer> toUserIds;

    /**
     * 附件
     */
    private List<Attachment> attachmentList;

    /**
     * 是否推送消息：0.否，1.是
     */
    private Integer isPush;

    /**
     * 数据
     */
    private Map<String, Object> data;
}
