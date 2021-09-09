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
public class BasicMessage implements NumberIdEntity<Integer> {

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
     * 备注
     */
    private String remark;

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTypeName() {
        return NameEnumUtils.getName(this.type, MessageType.class);
    }
}
