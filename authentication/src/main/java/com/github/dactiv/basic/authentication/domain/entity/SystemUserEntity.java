package com.github.dactiv.basic.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.github.dactiv.basic.commons.authentication.IdRoleAuthority;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.mybatis.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 用户实体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class SystemUserEntity implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 750742816513263456L;
    /**
     * 密码字段名称
     */
    public static final String PASSWORD_FIELD_NAME = "password";
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
     * 邮箱
     */
    @Email
    @Length(max = 64)
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @NotNull
    @Range(min = 1, max = 3)
    private UserStatus status;

    /**
     * 登录帐号
     */
    @NotEmpty
    @Length(max = 12)
    private String username;

    /**
     * 所属组集合
     */
    @JsonCollectionGenericType(IdRoleAuthority.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdRoleAuthority> groupsInfo = new LinkedList<>();

    /**
     * 独立权限资源 id 集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, List<String>> resourceMap = new LinkedHashMap<>();

}
