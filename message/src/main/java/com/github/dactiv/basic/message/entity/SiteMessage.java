package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;
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
public class SiteMessage extends TitleMessage {


    private static final long serialVersionUID = 2037280001998945900L;

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
     * 是否推送消息：0.否，1.是
     */
    private Integer isPush;

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> link;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 是否存在附件
     */
    private Integer hasAttachment = YesOrNo.No.getValue();

    /**
     * 附件信息
     */
    @TableField(exist = false)
    private List<Attachment> attachmentList;

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