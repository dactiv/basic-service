package com.github.dactiv.basic.message.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>邮件消息实体类</p>
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("emailMessage")
@TableName("tb_email_message")
public class EmailMessage implements Retryable, ExecuteStatus.Body, NumberIdEntity<Integer> {

    private static final long serialVersionUID = 8360029094205090328L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

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
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 状态：0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

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