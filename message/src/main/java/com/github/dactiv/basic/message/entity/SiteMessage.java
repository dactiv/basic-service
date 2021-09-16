package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息实体类</p>
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 03:48:46
 */
@Data
@NoArgsConstructor
@Alias("siteMessage")
@TableName("tb_site_message")
@EqualsAndHashCode(callSuper = true)
public class SiteMessage extends BasicMessage implements AttachmentMessage, Retryable, ExecuteStatus.Body, BatchMessage.Body {

    private static final long serialVersionUID = 2037280001998945900L;

    /**
     * 标题
     */
    private String title;
    /**
     * 渠道商
     */
    private String channel;

    /**
     * 收到的用户 id
     */
    private Integer toUserId;

    /**
     * 是否推送消息：0.否，1.是
     */
    private Integer isPush = YesOrNo.Yes.getValue();

    /**
     * 是否已读：0.否，1.是
     */
    private Integer isRead = YesOrNo.No.getValue();

    /**
     * 读取时间
     */
    private Date readTime;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> meta;
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
     * 是否存在附件
     */
    private Integer hasAttachment = YesOrNo.No.getValue();

    /**
     * 附件信息
     */
    @TableField(exist = false)
    private List<Attachment> attachmentList = new LinkedList<>();

    @Override
    public void setExecuteStatus(ExecuteStatus status) {
        this.setStatus(status.getValue());
    }

    /**
     * 获取是否已读名称
     *
     * @return 是否已读名称
     */
    public String getIsReadName() {
        return NameValueEnumUtils.getName(this.isRead, YesOrNo.class);
    }

    /**
     * 获取是否存在附件名称
     *
     * @return 是否存在附件名称
     */
    public String getHasAttachmentName() {
        return NameValueEnumUtils.getName(this.hasAttachment, YesOrNo.class);
    }

    /**
     * 获取是否推送名称
     *
     * @return 是否推送名称
     */
    public String getIsPushName() {
        return NameValueEnumUtils.getName(this.isPush, YesOrNo.class);
    }
}