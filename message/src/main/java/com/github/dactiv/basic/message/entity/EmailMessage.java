package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;
import java.util.List;

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
public class EmailMessage implements Retryable, ExecuteStatus.Body, NumberIdEntity<Integer>, BatchMessage.Body {

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
     * 发送邮件
     */
    private String fromEmail;

    /**
     * 收取邮件
     */
    private String toEmail;

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
     * 是否存在附件
     */
    private Integer hasAttachment = YesOrNo.No.getValue();

    /**
     * 批量消息 id
     */
    private Integer batchId;

    /**
     * 附件信息
     */
    @TableField(exist = false)
    private List<Attachment> attachmentList;

    /**
     * 备注
     */
    private String remark;

    @Override
    public void setExecuteStatus(ExecuteStatus status) {
        this.setStatus(status.getValue());
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(this.status, ExecuteStatus.class);
    }

    /**
     * 获取是否存在附件名称
     *
     * @return 是否存在附件名称
     */
    public String getHasAttachmentName() {
        return NameValueEnumUtils.getName(this.hasAttachment, YesOrNo.class);
    }
}