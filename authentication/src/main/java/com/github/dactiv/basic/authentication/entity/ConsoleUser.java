package com.github.dactiv.basic.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>系统用户实体类</p>
 * <p>Table: tb_console_user - 系统用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("consoleUser")
@TableName("tb_console_user")
public class ConsoleUser implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 542256170672538050L;
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
     * 获取用户状态名称
     *
     * @return 用户状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(status, UserStatus.class);
    }
}