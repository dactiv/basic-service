package com.github.dactiv.basic.message.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.basic.message.domain.AttachmentMessage;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.LinkedList;
import java.util.List;

/**
 * 邮件消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailMessageBody extends BasicMessageEntity implements AttachmentMessage {

    private static final long serialVersionUID = -1367698344075208239L;

    /**
     * 标题
     */
    private String title;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> toEmails = new LinkedList<>();

    /**
     * 附件
     */
    @Valid
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<AttachmentEntity> attachmentList = new LinkedList<>();
}
