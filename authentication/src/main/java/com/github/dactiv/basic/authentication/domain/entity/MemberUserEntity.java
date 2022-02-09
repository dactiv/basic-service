package com.github.dactiv.basic.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.basic.authentication.domain.meta.MemberUserInitializationMeta;
import com.github.dactiv.basic.authentication.security.MemberUserDetailsService;
import com.github.dactiv.framework.commons.jackson.serializer.DesensitizeSerializer;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * <p>会员用户实体类</p>
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@NoArgsConstructor
@Alias("memberUser")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_member_user", autoResultMap = true)
public class MemberUserEntity extends SystemUserEntity {

    private static final long serialVersionUID = 2708129983369081033L;

    /**
     * 注册时间
     */
    private Date registrationTime = new Date();

    /**
     * 手机号码
     */
    @NotEmpty
    @Length(max = 24)
    @Pattern(regexp = MemberUserDetailsService.IS_MOBILE_PATTERN_STRING)
    private String phone;

    /**
     * 用户初始化
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MemberUserInitializationMeta initialization;


    /**
     * 获取加敏手机号码
     *
     * @return 加敏手机号码
     */
    @JsonSerialize(using = DesensitizeSerializer.class)
    public String getDesensitizePhone() {
        return phone;
    }
}