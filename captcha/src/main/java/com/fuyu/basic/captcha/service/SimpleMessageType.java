package com.fuyu.basic.captcha.service;

import javax.validation.constraints.NotNull;

/**
 * 简单的消息类型实体实现
 *
 * @author maurice
 */
public class SimpleMessageType implements MessageType {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private String messageType;

    /**
     * 简单的消息类型实体实现
     */
    public SimpleMessageType() {
    }

    /**
     * 简单的消息类型实体实现
     *
     * @param messageType 消息类型
     */
    public SimpleMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * 设置消息类型
     *
     * @param messageType 消息类型
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @Override
    public String getMessageType() {
        return messageType;
    }
}
