package com.github.dactiv.basic.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>用户组实体类</p>
 * <p>Table: tb_group - 用户组表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("group")
@NoArgsConstructor
@EqualsAndHashCode
@TableName("tb_group")
public class Group implements Tree<Integer, Group>, NumberIdEntity<Integer> {

    private static final long serialVersionUID = 5357157352791368716L;

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
     * 名称
     */
    @NotEmpty
    @Length(max = 32)
    private String name;

    /**
     * spring security role 的 authority 值
     */
    @NotEmpty
    @Length(max = 64)
    private String authority;

    /**
     * 来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     */
    @NotEmpty
    @Length(max = 128)
    private String source;

    /**
     * 父类 id
     */
    private Integer parentId;

    /**
     * 是否可删除:0.否、1.是
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer removable;

    /**
     * 是否可修改:0.否、1.是
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer modifiable;

    /**
     * 状态:0.禁用、1.启用
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, Group>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, Group> parent) {
        Group group = Casts.cast(parent);
        return Objects.equals(group.getId(), this.getParent());
    }

    /**
     * 获取状态名称
     *
     * @return String
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(this.status, DisabledOrEnabled.class);
    }

    /**
     * 获取是否可删除名称
     *
     * @return String
     */
    public String getRemovableName() {
        return NameValueEnumUtils.getName(removable, YesOrNo.class);
    }

    /**
     * 获取是否可修改名称
     *
     * @return String
     */
    public String getModifiableName() {
        return NameValueEnumUtils.getName(modifiable, YesOrNo.class);
    }

    /**
     * 获取来源名称
     *
     * @return String
     */
    public List<String> getSourceName() {
        return Arrays.stream(StringUtils.split(source, ","))
                .map(StringUtils::trim)
                .map(s -> NameEnumUtils.getName(s, ResourceSource.class))
                .collect(Collectors.toList());
    }

}