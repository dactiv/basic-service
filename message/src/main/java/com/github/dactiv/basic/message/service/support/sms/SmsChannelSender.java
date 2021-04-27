package com.github.dactiv.basic.message.service.support.sms;

import com.github.dactiv.basic.message.dao.entity.SmsMessage;
import com.github.dactiv.framework.commons.RestResult;

import java.util.Map;

/**
 * 短信渠道发送者
 *
 * @author maurice
 */
public interface SmsChannelSender {

    /**
     * 获取短信渠道类型
     *
     * @return 短信渠道类型
     */
    String getType();

    /**
     * 获取短信渠道名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 发送短信
     *
     * @param entity 短信实体
     * @return rest 结果集
     */
    RestResult<Map<String, Object>> sendSms(SmsMessage entity);

    /**
     * 获取可用余额
     *
     * @return 可用余额
     */
    SmsBalance getBalance();
}
