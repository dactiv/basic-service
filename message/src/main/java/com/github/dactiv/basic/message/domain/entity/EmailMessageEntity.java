package com.github.dactiv.basic.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.basic.message.domain.AttachmentMessage;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>邮件消息实体类</p>
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("emailMessage")
@TableName("tb_email_message")
@EqualsAndHashCode(callSuper = true)
public class EmailMessageEntity extends BasicMessageEntity implements AttachmentMessage, Retryable, ExecuteStatus.Body, BatchMessageEntity.Body {

    private static final long serialVersionUID = 8360029094205090328L;

    /**
     * 标题
     */
    private String title;

    /**
     * 发送邮件
     */
    @NotNull
    private String fromEmail;

    /**
     * 收取邮件
     */
    private String toEmail;

    /**
     * 是否存在附件
     */
    private YesOrNo hasAttachment = YesOrNo.No;
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
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 批量消息 id
     */
    private Integer batchId;

    /**
     * 附件信息
     */
    @TableField(exist = false)
    private List<AttachmentEntity> attachmentList = new LinkedList<>();

}