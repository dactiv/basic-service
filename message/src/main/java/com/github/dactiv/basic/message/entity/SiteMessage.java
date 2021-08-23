package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SiteMessage implements Retryable, ExecuteStatus.Body, NumberIdEntity<Integer>, BatchMessage.Body {


    private static final long serialVersionUID = 2037280001998945900L;
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
     * 状态：0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

    /**
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 批量消息 id
     */
    private Integer batchId;

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
     * 获取是否已读名称
     *
     * @return 是否已读名称
     */
    public String getIsReadName() {
        return NameValueEnumUtils.getName(this.isRead, YesOrNo.class);
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