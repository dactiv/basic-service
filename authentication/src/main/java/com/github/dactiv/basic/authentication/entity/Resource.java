package com.github.dactiv.basic.authentication.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>资源实体类</p>
 * <p>Table: tb_resource - 资源表</p>
 *
 * @author maurice
 * @since 2020-04-13 09:48:05
 */
@Data
@Alias("resource")
@NoArgsConstructor
@TableName("tb_resource")
@EqualsAndHashCode(callSuper = false)
public class Resource extends IdEntity<Integer> implements Tree<Integer, Resource> {

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
    private String type;

    /**
     * 来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     */
    @NotEmpty
    @Length(max = 16)
    @EqualsAndHashCode.Include
    private List<String> sources;

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
    private Integer parentId;

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
    private List<Tree<Integer, Resource>> children = new ArrayList<>();

    /**
     * 获取类型名称
     *
     * @return String
     */
    public String getTypeName() {
        return NameEnumUtils.getName(this.type, ResourceType.class);
    }

    /**
     * 获取类型名称
     *
     * @return String
     */
    public List<String> getSourcesName() {
        if (CollectionUtils.isEmpty(this.sources)){
            return null;
        }
        return this.sources.stream().map(s -> NameEnumUtils.getName(s, ResourceSource.class)).collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, Resource> parent) {
        Resource resource = Casts.cast(parent);
        return Objects.equals(resource.getId(), this.getParent());
    }
}