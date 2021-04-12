package com.fuyu.basic.message.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.support.ExecuteStatus;
import com.fuyu.basic.commons.enumerate.support.YesOrNo;
import com.fuyu.basic.commons.retry.Retryable;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>站内信消息实体类</p>
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 03:48:46
 */
@Alias("siteMessage")
public class SiteMessage extends IntegerIdEntity implements Retryable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型
     */
    private String type;

    /**
     * 渠道商
     */
    private String channel;

    /**
     * 发送的用户 id
     */
    private Integer fromUserId;

    /**
     * 收到的用户 id
     */
    private Integer toUserId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 是否推送消息：0.否，1.是
     */
    private Integer pushMessage;

    /**
     * 是否已读：0.否，1.是
     */
    private Integer isRead = YesOrNo.No.getValue();

    /**
     * 读取时间
     */
    private Date readTime;

    /**
     * 链接
     */
    private Map<String, Object> link;

    /**
     * 数据
     */
    private Map<String, Object> data;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 0;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 状态：0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

    /**
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 站内信消息实体类
     */
    public SiteMessage() {
    }

    /**
     * 获取类型
     *
     * @return String
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取渠道商
     *
     * @return String
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * 设置渠道商
     *
     * @param channel 渠道商
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * 获取发送的用户 id
     *
     * @return Integer
     */
    public Integer getFromUserId() {
        return this.fromUserId;
    }

    /**
     * 设置发送的用户 id
     *
     * @param fromUserId 发送的用户 id
     */
    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    /**
     * 获取收到的用户 id
     *
     * @return Integer
     */
    public Integer getToUserId() {
        return this.toUserId;
    }

    /**
     * 设置收到的用户 id
     *
     * @param toUserId 收到的用户 id
     */
    public void setToUserId(Integer toUserId) {
        this.toUserId = toUserId;
    }

    /**
     * 获取标题
     *
     * @return String
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取内容
     *
     * @return String
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取是否推送消息：0.否，1.是
     *
     * @return Integer
     */
    public Integer getPushMessage() {
        return this.pushMessage;
    }

    /**
     * 设置是否推送消息：0.否，1.是
     *
     * @param pushMessage 是否推送消息：0.否，1.是
     */
    public void setPushMessage(Integer pushMessage) {
        this.pushMessage = pushMessage;
    }

    /**
     * 获取是否已读：0.否，1.是
     *
     * @return Integer
     */
    public Integer getIsRead() {
        return this.isRead;
    }

    /**
     * 设置是否已读：0.否，1.是
     *
     * @param isRead 是否已读：0.否，1.是
     */
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    /**
     * 获取读取时间
     *
     * @return Date
     */
    public Date getReadTime() {
        return this.readTime;
    }

    /**
     * 设置读取时间
     *
     * @param readTime 读取时间
     */
    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    /**
     * 获取链接
     *
     * @return String
     */
    public Map<String, Object> getLink() {
        return this.link;
    }

    /**
     * 设置链接
     *
     * @param link 链接
     */
    public void setLink(Map<String, Object> link) {
        this.link = link;
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 设置数据
     *
     * @param data 数据
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * 获取重试次数
     *
     * @return Integer
     */
    @Override
    public Integer getRetryCount() {
        return this.retryCount;
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    @Override
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetryCount 最大重试次数
     */
    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取最后发送时间
     *
     * @return Date
     */
    public Date getLastSendTime() {
        return this.lastSendTime;
    }

    /**
     * 设置最后发送时间
     *
     * @param lastSendTime 最后发送时间
     */
    public void setLastSendTime(Date lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    /**
     * 获取异常信息
     *
     * @return String
     */
    public String getException() {
        return this.exception;
    }

    /**
     * 设置异常信息
     *
     * @param exception 异常信息
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * 获取状态：0.执行中、1.执行成功，99.执行失败
     *
     * @return Integer
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 设置状态：0.执行中、1.执行成功，99.执行失败
     *
     * @param status 状态：0.执行中、1.执行成功，99.执行失败
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取发送成功时间
     *
     * @return Date
     */
    public Date getSuccessTime() {
        return this.successTime;
    }

    /**
     * 设置发送成功时间
     *
     * @param successTime 发送成功时间
     */
    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    /**
     * 获取备注
     *
     * @return String
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取唯一索引查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public Map<String, Object> getUniqueFilter() {
        Map<String, Object> filter = new LinkedHashMap<>();
        return filter;
    }

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //
}