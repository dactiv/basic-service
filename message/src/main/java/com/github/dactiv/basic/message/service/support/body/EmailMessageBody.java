package com.github.dactiv.basic.message.service.support.body;

import com.github.dactiv.basic.message.entity.TitleMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
public class EmailMessageBody extends TitleMessage {

    private static final long serialVersionUID = -1367698344075208239L;

    /**
     * 发件方
     */
    private String fromEmail;

    /**
     * 收件方集合
     */
    private List<String> toEmails = new LinkedList<>();
}
