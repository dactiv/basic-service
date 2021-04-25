package com.github.dactiv.basic.authentication.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>用户初始化实体类</p>
 * <p>Table: tb_member_user_initialization - 用户初始化表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:45
 */
@Data
@EqualsAndHashCode
@Alias("memberUserInitialization")
@TableName("tb_member_user_initialization")
public class MemberUserInitialization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 最后更新时间
     */
    @Version
    @JsonIgnore
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

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
    private Integer modifyPassword = YesOrNo.No.getValue();

    /**
     * 是否客更新登录账户：1.是、0.否
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer modifyUsername = YesOrNo.No.getValue();

    /**
     * 会否绑定邮箱：1.是，0.否
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer buildEmail = YesOrNo.No.getValue();
}