package com.github.dactiv.basic.message.service;

import com.github.dactiv.framework.commons.RestResult;

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
    RestResult<Object> send(Map<String, Object> request) throws Exception;

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getMessageType();
}
