package com.github.dactiv.basic.socket.server.service.chat;

import com.github.dactiv.basic.socket.server.domain.GlobalMessagePage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;

import java.util.Date;
import java.util.List;

/**
 * 消息操作, 用于针对消息类型，做消息的展示、发送、读取的处理
 *
 * @author maurice.chen
 */
public interface MessageOperation {

    /**
     * 聊天信息事件名称
     */
    String CHAT_MESSAGE_EVENT_NAME = "chat_message";
    /**
     * 聊天信息读取事件名称
     */
    String CHAT_READ_MESSAGE_EVENT_NAME = "chat_read_message";

    /**
     * 是否支持消息类型
     *
     * @param type 消息类型
     *
     * @return true 是，否则 false
     */
    boolean isSupport(MessageTypeEnum type);

    /**
     * 获取消息分页
     *
     * @param userId      用户 id
     * @param targetId    目标 id（对方用户 id/ 群聊 id）
     * @param pageRequest 分页请求
     *
     * @return 全局消息分页
     */
    GlobalMessagePage getHistoryMessagePage(Integer userId, Integer targetId, Date time, ScrollPageRequest pageRequest);

    /**
     * 获取历史消息日期集合
     *
     * @param userId   用户 id
     * @param targetId 目标 id（对方用户 id/ 群聊 id）
     *
     * @return 历史消息日期集合
     */
    List<Date> getHistoryMessageDateList(Integer userId, Integer targetId);

    /**
     * 读取信息
     *
     * @param body 读取消息 request body
     *
     * @throws Exception 执行时错误时抛出
     */
    void readMessage(ReadMessageRequestBody body) throws Exception;

    /**
     * 消费异步读取信息的队列消息
     *
     * @param body 读取消息 request body
     *
     * @throws Exception 执行时错误时抛出
     */
    void consumeReadMessage(ReadMessageRequestBody body) throws Exception;

    /**
     * 发送消息
     *
     * @param senderId    发送者用户 id
     * @param recipientId 接受者 id
     * @param content     消息内容
     *
     * @return 消息实体
     *
     * @throws Exception 执行时错误时抛出
     */
    BasicMessageMeta.Message sendMessage(Integer senderId, Integer recipientId, String content) throws Exception;
}
