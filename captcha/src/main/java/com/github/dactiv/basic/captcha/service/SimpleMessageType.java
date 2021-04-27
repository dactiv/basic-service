package com.github.dactiv.basic.captcha.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 简单的消息类型实体实现
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMessageType implements MessageType {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private String messageType;
}
