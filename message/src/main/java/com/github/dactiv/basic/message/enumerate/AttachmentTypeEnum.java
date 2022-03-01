package com.github.dactiv.basic.message.enumerate;

import com.github.dactiv.basic.message.domain.body.EmailMessageBody;
import com.github.dactiv.basic.message.domain.body.SiteMessageBody;
import com.github.dactiv.basic.message.domain.body.SmsMessageBody;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
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
public enum AttachmentTypeEnum implements NameValueEnum<Integer> {

    /**
     * 站内信
     */
    Site("站内信", 10, SiteMessageBody.class),
    /**
     * 邮件
     */
    Email("邮件", 20, EmailMessageBody.class);

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final Integer value;

    /**
     * 类类型
     */
    private final Class<? extends BasicMessageEntity> type;

    /**
     * 通过类类型获取枚举内容
     *
     * @param type 类类型
     *
     * @return 实际枚举只
     */
    public static AttachmentTypeEnum valueOf(Class<? extends BasicMessageEntity> type) {

        for (AttachmentTypeEnum t : AttachmentTypeEnum.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }

        throw new SystemException("找不到类型为 [" + type + "] 的枚举内容");
    }
}
