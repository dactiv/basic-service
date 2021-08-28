package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * <p>短信消息实体类</p>
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("smsMessage")
@TableName("tb_sms_message")
@EqualsAndHashCode(callSuper = true)
public class SmsMessage extends BasicMessage {

    private static final long serialVersionUID = 3229810529789017287L;

    /**
     * 渠道名称
     */
    private String channel;

    /**
     * 手机号码
     */
    private String phoneNumber;
}