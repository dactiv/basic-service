package com.fuyu.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.NameEnumUtils;
import com.fuyu.basic.commons.tree.Tree;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
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
@Alias("resource")
public class Resource extends IntegerIdEntity implements Tree<Integer, Resource> {

    private static final long serialVersionUID = 1L;

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
    private String applicationName;

    /**
     * 类型:MENU.菜单类型、SECURITY.安全类型
     */
    @NotEmpty
    @Length(max = 16)
    private String type;

    /**
     * 来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     */
    @NotEmpty
    @Length(max = 16)
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
     * 资源实体类
     */
    public Resource() {
    }

    public Resource(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 获取名称
     *
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取唯一识别
     *
     * @return String
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置唯一识别
     *
     * @param code 唯一识别
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取应用名称
     *
     * @return String
     */
    public String getApplicationName() {
        return this.applicationName;
    }

    /**
     * 设置应用名称
     *
     * @param applicationName 应用名称
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 获取类型:MENU.菜单类型、SECURITY.安全类型
     *
     * @return String
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置类型:MENU.菜单类型、SECURITY.安全类型
     *
     * @param type 类型:MENU.菜单类型、SECURITY.安全类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     *
     * @return String
     */
    public String getSource() {
        return this.source;
    }

    /**
     * 设置来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     *
     * @param source 来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取版本号
     *
     * @return String
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取spring security 拦截值
     *
     * @return String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * 设置spring security 拦截值
     *
     * @param value spring security 拦截值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取spring security 资源的 authority 值
     *
     * @return String
     */
    public String getAuthority() {
        return this.authority;
    }

    /**
     * 设置spring security 资源的 authority 值
     *
     * @param authority spring security 资源的 authority 值
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    /**
     * 获取图标
     *
     * @return String
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * 设置图标
     *
     * @param icon 图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取父类 id
     *
     * @return Integer
     */
    public Integer getParentId() {
        return this.parentId;
    }

    /**
     * 设置父类 id
     *
     * @param parentId 父类 id
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取状态:0.禁用、1.启用
     *
     * @return Integer
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 设置状态:0.禁用、1.启用
     *
     * @param status 状态:0.禁用、1.启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取顺序值
     *
     * @return Integer
     */
    public Integer getSort() {
        return this.sort;
    }

    /**
     * 设置顺序值
     *
     * @param sort 顺序值
     */
    public void setSort(Integer sort) {
        this.sort = sort;
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
        if (Objects.nonNull(this.code) && !"".equals(this.code)) {
            filter.put("codeEq", getCode());
        }
        if (Objects.nonNull(this.applicationName) && !"".equals(this.applicationName)) {
            filter.put("applicationNameEq", getApplicationName());
        }
        if (Objects.nonNull(this.type) && !"".equals(this.type)) {
            filter.put("typeEq", getType());
        }
        if (Objects.nonNull(this.source) && !"".equals(this.source)) {
            filter.put("sourceEq", getSource());
        }
        if (Objects.nonNull(this.parentId) && !"".equals(this.parentId.toString())) {
            filter.put("parentIdEq", getParentId());
        }
        return filter;
    }

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //

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
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
     * 设置子节点
     *
     * @param children 子节点
     */
    public void setChildren(List<Tree<Integer, Resource>> children) {
        this.children = children;
    }

    @Override
    public List<Tree<Integer, Resource>> getChildren() {
        return children;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource resource = (Resource) o;

        return Objects.equals(toString(), resource.toString());
    }

    @Override
    public String toString() {
        return "[" + getApplicationName() + "]_"
                + getCode()
                + "_"
                + getType()
                + "_"
                + getSource()
                + (Objects.isNull(getParent()) ? "" : "_" + getParent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }
}