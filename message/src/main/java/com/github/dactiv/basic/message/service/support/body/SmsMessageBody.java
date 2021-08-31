package com.github.dactiv.basic.message.service.support.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.basic.message.entity.BasicMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsMessageBody extends BasicMessage {

    private static final long serialVersionUID = -6678810630364920364L;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> phoneNumbers = new LinkedList<>();
}
