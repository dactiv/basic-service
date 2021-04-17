package com.github.dactiv.basic.message.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.IntegerIdEntity;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.retry.Retryable;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>邮件消息实体类</p>
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Alias("emailMessage")
public class EmailMessage extends IntegerIdEntity implements Retryable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型
     */
    private String type;

    /**
     * 发送用户邮件
     */
    private String fromUser;

    /**
     * 收取用户邮件
     */
    private String toUser;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 重试次数
     */
    private Integer retryCount= 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 0;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 状态：0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

    /**
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 备注
     */
    private String remark;

    /**
     * 邮件消息实体类
     */
    public EmailMessage() {
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
     * 获取发送用户邮件
     *
     * @return String
     */
    public String getFromUser() {
        return this.fromUser;
    }

    /**
     * 设置发送用户邮件
     *
     * @param fromUser 发送用户邮件
     */
    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    /**
     * 获取收取用户邮件
     *
     * @return String
     */
    public String getToUser() {
        return this.toUser;
    }

    /**
     * 设置收取用户邮件
     *
     * @param toUser 收取用户邮件
     */
    public void setToUser(String toUser) {
        this.toUser = toUser;
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
     * 获取重试次数
     *
     * @return Integer
     */
    @Override
    public Integer getRetryCount() {
        return this.retryCount;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 这是最大重试次数
     *
     * @param maxRetryCount 最大重试次数
     */
    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
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