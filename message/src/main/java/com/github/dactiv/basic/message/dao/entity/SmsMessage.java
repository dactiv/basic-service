package com.github.dactiv.basic.message.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>短信消息实体类</p>
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("smsMessage")
@TableName("tb_sms_message")
public class SmsMessage implements Retryable, ExecuteStatus.Body, Serializable {

    private static final long serialVersionUID = -3229810529789017287L;
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
     * 渠道名称
     */
    private String channel;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 内容
     */
    private String content;

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
     * 状态：0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

    /**
     * 成功时间
     */
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime successTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 备注
     */
    private String remark;

    @Override
    public void setExecuteStatus(ExecuteStatus status) {
        this.setStatus(status.getValue());
    }
}