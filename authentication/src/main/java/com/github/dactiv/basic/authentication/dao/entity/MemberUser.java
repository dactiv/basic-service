package com.github.dactiv.basic.authentication.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.commons.jackson.JacksonDesensitize;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>会员用户实体类</p>
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@EqualsAndHashCode
@Alias("memberUser")
@TableName("tb_member_user")
public class MemberUser implements Serializable {

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
     * 登录帐号
     */
    @NotEmpty
    @Length(max = 32)
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 邮箱
     */
    @Email
    private String email;

    /**
     * 手机号码
     */
    @NotEmpty
    @Pattern(
            regexp = "/^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$/"
    )
    @Length(max = 24)
    @JsonSerialize(using = JacksonDesensitize.class)
    private String phone;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @NotNull
    @Range(min = 1, max = 3)
    private Integer status;

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //

    /**
     * 获取用户状态名称
     *
     * @return 用户状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(status, UserStatus.class);
    }

    /**
     * 用户初始化
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private MemberUserInitialization initialization;

    /**
     * 获取用户初始化信息
     *
     * @return 用户初始化信息
     */
    public MemberUserInitialization getInitialization() {
        return initialization;
    }

    /**
     * 设置用户初始化信息
     *
     * @param initialization 用户初始化信息
     */
    public void setInitialization(MemberUserInitialization initialization) {
        this.initialization = initialization;
    }
}