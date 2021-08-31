package com.github.dactiv.basic.message.enumerate;

import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.basic.message.service.support.body.EmailMessageBody;
import com.github.dactiv.basic.message.service.support.body.SiteMessageBody;
import com.github.dactiv.basic.message.service.support.body.SmsMessageBody;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentType implements NameValueEnum<Integer> {

    /**
     * 站内信
     */
    Site("站内信",10, SiteMessageBody.class),
    /**
     * 邮件
     */
    Email("邮件", 20, EmailMessageBody.class),
    /**
     * 短信
     */
    Sms("短信", 30, SmsMessageBody.class);

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private Integer value;

    /**
     * 类类型
     */
    private Class<? extends BatchMessage.Body> type;

    /**
     * 通过类类型获取枚举内容
     *
     * @param type 类类型
     *
     * @return 实际枚举只
     */
    public static AttachmentType valueOf(Class<? extends BatchMessage.Body> type) {

        for (AttachmentType t : AttachmentType.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }

        throw new SystemException("找不到类型为 [" + type + "] 的枚举内容");
    }
}
