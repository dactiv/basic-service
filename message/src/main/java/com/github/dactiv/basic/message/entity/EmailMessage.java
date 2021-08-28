package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
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
public class EmailMessage extends TitleMessage {

    private static final long serialVersionUID = 8360029094205090328L;

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

    @Override
    public void setExecuteStatus(ExecuteStatus status) {
        this.setStatus(status.getValue());
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