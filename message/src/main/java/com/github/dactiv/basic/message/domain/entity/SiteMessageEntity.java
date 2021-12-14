package com.github.dactiv.basic.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.basic.message.domain.AttachmentMessage;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.handler.NameValueEnumTypeHandler;
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
public class SiteMessageEntity extends BasicMessageEntity implements AttachmentMessage, Retryable, ExecuteStatus.Body, BatchMessageEntity.Body {

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
    private YesOrNo isPush = YesOrNo.Yes;

    /**
     * 是否已读：0.否，1.是
     */
    private YesOrNo isRead = YesOrNo.No;

    /**
     * 读取时间
     */
    private Date readTime;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
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
    private YesOrNo hasAttachment = YesOrNo.No;

    /**
     * 附件信息
     */
    @TableField(exist = false)
    private List<AttachmentEntity> attachmentList = new LinkedList<>();

    public SiteMessageEntity(Integer id) {
        this.setId(id);
    }
}