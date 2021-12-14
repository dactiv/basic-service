package com.github.dactiv.basic.authentication.domain.meta;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>资源实体类</p>
 * <p>Table: tb_resource - 资源表</p>
 *
 * @author maurice
 * @since 2020-04-13 09:48:05
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResourceMeta extends IdEntity<String> implements Tree<String, ResourceMeta> {

    private static final long serialVersionUID = 4709419291009298510L;

    /**
     * 名称
     */
    @NotEmpty
    @Length(max = 32)
    private String name;

    /**
     * 应用名称
     */
    @NotEmpty
    @Length(max = 64)
    @EqualsAndHashCode.Include
    private String applicationName;

    /**
     * 唯一识别
     */
    @NotEmpty
    @Length(max = 128)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * 类型:MENU.菜单类型、SECURITY.安全类型
     */
    @NotEmpty
    @Length(max = 16)
    @EqualsAndHashCode.Include
    private ResourceType type;

    /**
     * 来源
     *
     * @see ResourceSourceEnum
     */
    @NotEmpty
    @Length(max = 16)
    @EqualsAndHashCode.Include
    private List<ResourceSourceEnum> sources;

    /**
     * 版本号
     */
    @NotEmpty
    @Length(max = 16)
    private String version;

    /**
     * spring security 拦截值
     */
    @Length(max = 256)
    private String value;

    /**
     * spring security 资源的 authority 值
     */
    @Length(max = 64)
    private String authority;

    /**
     * 图标
     */
    private String icon;

    /**
     * 父类 id
     */
    @EqualsAndHashCode.Include
    private String parentId;

    /**
     * 顺序值
     */
    @Range(min = 0, max = 999)
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<String, ResourceMeta>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public String getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<String, ResourceMeta> parent) {
        ResourceMeta resource = Casts.cast(parent);
        return Objects.equals(resource.getId(), this.getParent());
    }
}