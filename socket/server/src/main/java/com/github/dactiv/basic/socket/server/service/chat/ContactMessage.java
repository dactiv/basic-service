package com.github.dactiv.basic.socket.server.service.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.socket.server.enitty.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContactMessage extends Contact {
    private static final long serialVersionUID = 6725391155534568648L;

    /**
     * 发送者  id
     */
    @JsonIgnore
    private Integer recipientId;
}
