package com.github.dactiv.basic.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * <p>资源实体类</p>
 * <p>Table: tb_resource - 资源表</p>
 *
 * @author maurice
 * @since 2020-04-13 09:48:05
 */
@Data
@Alias("resource")
@EqualsAndHashCode
@NoArgsConstructor
@TableName("tb_resource")
public class Resource implements NumberIdEntity<Integer>, Tree<Integer, Resource> {

    private static final long serialVersionUID = 4709419291009298510L;

    /**
     * 所有自动配置的可用来源
     */
    public static final List<String> DEFAULT_ALL_SOURCE_VALUES = Arrays.asList(
            ResourceSource.All.toString(),
            ResourceSource.Console.toString(),
            ResourceSource.Front.toString(),
            ResourceSource.Mobile.toString(),
            ResourceSource.UserCenter.toString(),
            ResourceSource.System.toString()
    );

    public static final List<String> DEFAULT_CONTAIN_SOURCE_VALUES = Arrays.asList(
            ResourceSource.All.toString(),
            ResourceSource.System.toString()
    );

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
     * 唯一识别
     */
    @NotEmpty
    @Length(max = 128)
    private String code;

    /**
     * 应用名称
     */
    @NotEmpty
    @Length(max = 64)
    @EqualsAndHashCode.Include
    private String applicationName;

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
    private String source;

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
     * 状态:0.禁用、1.启用
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer status;

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
    public String getSourceName() {
        return NameEnumUtils.getName(this.source, ResourceSource.class);
    }

    /**
     * 获取状态名称
     *
     * @return String
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(this.status, DisabledOrEnabled.class);
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

    @JsonIgnore
    public Wrapper<Resource> getUniqueWrapper() {
        LambdaQueryWrapper<Resource> wrapper = Wrappers.lambdaQuery();

        if (Objects.nonNull(this.code) && !"".equals(this.code)) {
            wrapper.eq(Resource::getCode, this.code);
        }
        if (Objects.nonNull(this.applicationName) && !"".equals(this.applicationName)) {
            wrapper.eq(Resource::getApplicationName, this.applicationName);
        }
        if (Objects.nonNull(this.type) && !"".equals(this.type)) {
            wrapper.eq(Resource::getType, this.type);
        }
        if (Objects.nonNull(this.source) && !"".equals(this.source)) {
            wrapper.eq(Resource::getSource, this.source);
        }
        if (Objects.nonNull(this.parentId)) {
            wrapper.eq(Resource::getParentId, this.parentId);
        }

        return wrapper;
    }
}