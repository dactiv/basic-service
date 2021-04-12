package com.fuyu.basic.captcha.service.email;

import com.fuyu.basic.captcha.service.SimpleMessageType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 电子邮件实体
 *
 * @author maurice
 */
public class EmailEntity extends SimpleMessageType {

    /**
     * 电子邮件
     */
    @Email(message = "电子邮件格式不正确")
    @NotBlank(message = "电子邮件不能为空")
    private String email;

    /**
     * 邮件实体
     *
     * @param messageType 消息类型
     * @param email       电子邮件
     */
    public EmailEntity(String messageType, String email) {
        super(messageType);
        this.email = email;
    }

    /**
     * 邮件实体
     */
    public EmailEntity() {

    }

    /**
     * 获取电子邮件
     *
     * @return 电子邮件
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置电子邮件
     *
     * @param email 电子邮件
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
