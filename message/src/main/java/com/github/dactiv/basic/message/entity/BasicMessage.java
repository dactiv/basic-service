package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.message.enumerate.MessageType;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 基础消息实体，用于将所有消息内容公有化使用。
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class BasicMessage implements Retryable, ExecuteStatus.Body, NumberIdEntity<Integer>, BatchMessage.Body {

    private static final long serialVersionUID = -1167940666968537341L;

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
     *
     * @see com.github.dactiv.basic.message.enumerate.MessageType
     */
    @NotNull
    private String type;

    /**
     * 内容
     */
    @NotNull
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
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTypeName() {
        return NameEnumUtils.getName(this.type, MessageType.class);
    }
}
