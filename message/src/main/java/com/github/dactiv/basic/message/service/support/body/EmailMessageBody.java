package com.github.dactiv.basic.message.service.support.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.basic.message.entity.Attachment;
import com.github.dactiv.basic.message.entity.AttachmentMessage;
import com.github.dactiv.basic.message.entity.BasicMessage;
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
public class EmailMessageBody extends BasicMessage implements AttachmentMessage {

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
    private List<Attachment> attachmentList = new LinkedList<>();
}
