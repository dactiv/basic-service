package com.fuyu.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.support.YesOrNo;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>用户初始化实体类</p>
 * <p>Table: tb_member_user_initialization - 用户初始化表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:45
 */
@Alias("memberUserInitialization")
public class MemberUserInitialization extends IntegerIdEntity {

    private static final long serialVersionUID = 1L;

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

    /**
     * 用户初始化实体类
     */
    public MemberUserInitialization() {
    }

    /**
     * 获取用户 id
     *
     * @return Integer
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * 设置用户 id
     *
     * @param userId 用户 id
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取是否可更新密码：1.是、0.否
     *
     * @return Integer
     */
    public Integer getModifyPassword() {
        return this.modifyPassword;
    }

    /**
     * 设置是否可更新密码：1.是、0.否
     *
     * @param modifyPassword 是否可更新密码：1.是、0.否
     */
    public void setModifyPassword(Integer modifyPassword) {
        this.modifyPassword = modifyPassword;
    }

    /**
     * 获取是否客更新登录账户：1.是、0.否
     *
     * @return Integer
     */
    public Integer getModifyUsername() {
        return this.modifyUsername;
    }

    /**
     * 设置是否客更新登录账户：1.是、0.否
     *
     * @param modifyUsername 是否客更新登录账户：1.是、0.否
     */
    public void setModifyUsername(Integer modifyUsername) {
        this.modifyUsername = modifyUsername;
    }

    /**
     * 获取会否绑定邮箱：1.是，0.否
     *
     * @return Integer
     */
    public Integer getBuildEmail() {
        return this.buildEmail;
    }

    /**
     * 设置会否绑定邮箱：1.是，0.否
     *
     * @param buildEmail 会否绑定邮箱：1.是，0.否
     */
    public void setBuildEmail(Integer buildEmail) {
        this.buildEmail = buildEmail;
    }

    /**
     * 获取唯一索引查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public Map<String, Object> getUniqueFilter() {
        Map<String, Object> filter = new LinkedHashMap<>();
        if (Objects.nonNull(this.userId) && !"".equals(this.userId.toString())) {
            filter.put("userIdEq", getUserId());
        }
        return filter;
    }
}