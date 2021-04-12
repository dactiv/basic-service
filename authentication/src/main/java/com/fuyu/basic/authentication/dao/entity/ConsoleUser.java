package com.fuyu.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.NameValueEnumUtils;
import com.fuyu.basic.support.security.enumerate.UserStatus;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>后台用户实体类</p>
 * <p>Table: tb_console_user - 后台用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Alias("consoleUser")
public class ConsoleUser extends IntegerIdEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    @Email
    @Length(max = 64)
    private String email;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @NotNull
    @Range(min = 1, max = 3)
    private Integer status;

    /**
     * 登录帐号
     */
    @NotEmpty
    @Length(max = 12)
    private String username;

    /**
     * 真实姓名
     */
    @NotEmpty
    @Length(max = 16)
    private String realName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 后台用户实体类
     */
    public ConsoleUser() {
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
     * 获取真实姓名
     *
     * @return String
     */
    public String getRealName() {
        return this.realName;
    }

    /**
     * 设置真实姓名
     *
     * @param realName 真实姓名
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 获取备注
     *
     * @return String
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取唯一索引查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public Map<String, Object> getUniqueFilter() {
        Map<String, Object> filter = new LinkedHashMap<>();
        if (Objects.nonNull(this.email) && !"".equals(this.email)) {
            filter.put("emailEq", getEmail());
        }
        if (Objects.nonNull(this.username) && !"".equals(this.username)) {
            filter.put("usernameEq", getUsername());
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
}