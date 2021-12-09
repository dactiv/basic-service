package com.github.dactiv.basic.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.message.enumerate.MessageTypeEnum;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.mybatis.handler.NameValueEnumTypeHandler;
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
public class BasicMessageEntity implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = -1167940666968537341L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
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
     * @see MessageTypeEnum
     */
    @NotNull
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private MessageTypeEnum type;

    /**
     * 内容
     */
    @NotNull
    private String content;

    /**
     * 状态：0.执行中、1.执行成功，2.重试中，99.执行失败
     *
     * @see ExecuteStatus
     */
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 备注
     */
    private String remark;

}
