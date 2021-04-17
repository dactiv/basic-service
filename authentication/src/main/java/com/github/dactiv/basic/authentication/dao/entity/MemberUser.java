package com.github.dactiv.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.IntegerIdEntity;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.jackson.JacksonDesensitize;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>会员用户实体类</p>
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Alias("memberUser")
public class MemberUser extends IntegerIdEntity {

    private static final long serialVersionUID = 1L;

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

    /**
     * 会员用户实体类
     */
    public MemberUser() {
    }

    /**
     * 获取登录帐号
     *
     * @return String
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * 设置登录帐号
     *
     * @param username 登录帐号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return String
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取邮箱
     *
     * @return String
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * 设置邮箱
     *
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取手机号码
     *
     * @return String
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * 设置手机号码
     *
     * @param phone 备注
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取状态:1.启用、2.禁用、3.锁定
     *
     * @return Integer
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 设置状态:1.启用、2.禁用、3.锁定
     *
     * @param status 状态:1.启用、2.禁用、3.锁定
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取唯一索引查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public Map<String, Object> getUniqueFilter() {
        Map<String, Object> filter = new LinkedHashMap<>();
        if (Objects.nonNull(this.username) && !"".equals(this.username)) {
            filter.put("usernameEq", getUsername());
        }
        if (Objects.nonNull(this.email) && !"".equals(this.email)) {
            filter.put("emailEq", getEmail());
        }
        if (Objects.nonNull(this.phone) && !"".equals(this.phone)) {
            filter.put("phoneEq", getPhone());
        }
        return filter;
    }

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