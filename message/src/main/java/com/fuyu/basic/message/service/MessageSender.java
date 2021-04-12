package com.fuyu.basic.message.service;

import com.fuyu.basic.commons.spring.web.RestResult;

import java.util.Map;

/**
 * 消息发送者
 *
 * @author maurice
 */
public interface MessageSender {

    /**
     * 发送消息
     *
     * @param request http servlet request
     * @return rest 结果集
     * @throws Exception 发送错误时抛出
     */
    RestResult<Map<String, Object>> send(Map<String, Object> request) throws Exception;

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getMessageType();
}
