package com.github.dactiv.basic.authentication.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.mybatis.handler.NameValueEnumTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户初始化实体类
 *
 * @author maurice
 * @since 2020-04-13 10:14:45
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class MemberUserInitializationModel implements Serializable {

    private static final long serialVersionUID = 1714564243745969863L;
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
     * 版本号
     */
    @Version
    @JsonIgnore
    private Integer updateVersion = 1;

    /**
     * 用户 id
     */
    @NotNull
    @JsonIgnore
    private Integer userId;

    /**
     * 是否可更新密码：1.是、0.否
     */
    @NotNull
    @Range(min = 0, max = 1)
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private YesOrNo modifyPassword = YesOrNo.No;

    /**
     * 是否可更新登录账户：1.是、0.否
     */
    @NotNull
    @Range(min = 0, max = 1)
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private YesOrNo modifyUsername = YesOrNo.No;
}