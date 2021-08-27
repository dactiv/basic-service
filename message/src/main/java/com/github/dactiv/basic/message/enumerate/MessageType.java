package com.github.dactiv.basic.message.enumerate;

import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageType implements NameEnum {

    /**
     * 营销
     */
    Marketing("营销"),
    /**
     * 通知
     */
    Notice("通知"),
    /**
     * 警告
     */
    Warning("警告"),
    /**
     * 系统
     */
    System("系统");

    /**
     * 名称
     */
    private String name;

}
