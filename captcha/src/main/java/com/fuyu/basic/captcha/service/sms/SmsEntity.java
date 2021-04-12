package com.fuyu.basic.captcha.service.sms;

import com.fuyu.basic.captcha.service.SimpleMessageType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 短信验证码描述实体
 *
 * @author maurice
 */
public class SmsEntity extends SimpleMessageType {

    /**
     * 手机号码
     */
    @Pattern(
            regexp = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$",
            message = "手机号码格式错误"
    )
    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;

    /**
     * 短信实体
     */
    public SmsEntity() {
    }

    /**
     * 短信实体
     *
     * @param messageType 消息类型
     * @param phoneNumber 手机号码
     */
    public SmsEntity(String messageType, String phoneNumber) {
        super(messageType);
        this.phoneNumber = phoneNumber;
    }

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置手机号码
     *
     * @param phoneNumber 手机号码
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
