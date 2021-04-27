package com.github.dactiv.basic.message.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * <p>站内信消息实体类</p>
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 03:48:46
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("siteMessage")
@TableName("tb_site_message")
public class SiteMessage implements Retryable, ExecuteStatus.Body {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 更新版本号
     */
    @Version
    @JsonIgnore
    private Integer updateVersion = 1;

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
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime readTime;

    /**
     * 链接
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> link;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
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
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime lastSendTime;

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
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime successTime;

    /**
     * 备注
     */
    private String remark;

    @Override
    public void setExecuteStatus(ExecuteStatus status) {
        this.setStatus(status.getValue());
    }
}