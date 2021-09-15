package com.github.dactiv.basic.message.service.support.site;

import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.framework.commons.RestResult;

import java.util.Map;

/**
 * 消息推送渠道发送者
 *
 * @author maurice
 */
public interface SiteMessageChannelSender {

    /**
     * 获取消息推送渠道类型
     *
     * @return 站内信渠道类型
     */
    String getType();

    /**
     * 发送消息
     *
     * @param entity 站内信消息实体
     *
     * @return rest 结果集
     */
    RestResult<Map<String, Object>> sendSiteMessage(SiteMessage entity) throws Exception;
}
