package com.github.dactiv.basic.message.service.support.body;

import com.github.dactiv.basic.message.entity.BasicMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 短信 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsMessageBody extends BasicMessage {

    private static final long serialVersionUID = -6678810630364920364L;

    /**
     * 收件方集合
     */
    private List<String> phoneNumbers = new LinkedList<>();
}
