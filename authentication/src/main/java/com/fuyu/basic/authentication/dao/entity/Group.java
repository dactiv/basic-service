package com.fuyu.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.NameEnumUtils;
import com.fuyu.basic.commons.enumerate.NameValueEnumUtils;
import com.fuyu.basic.commons.enumerate.support.DisabledOrEnabled;
import com.fuyu.basic.commons.enumerate.support.YesOrNo;
import com.fuyu.basic.commons.tree.Tree;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
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
@Alias("group")
public class Group extends IntegerIdEntity implements Tree<Integer, Group> {

    private static final long serialVersionUID = 1L;

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
    private Integer canBeDelete;

    /**
     * 是否可修改:0.否、1.是
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer canBeModify;

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
     * 用户组实体类
     */
    public Group() {
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
     * 获取spring security role 的 authority 值
     *
     * @return String
     */
    public String getAuthority() {
        return this.authority;
    }

    /**
     * 设置spring security role 的 authority 值
     *
     * @param authority spring security role 的 authority 值
     */
    public void setAuthority(String authority) {
        this.authority = authority;
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
     * 获取是否可删除:0.否、1.是
     *
     * @return Integer
     */
    public Integer getCanBeDelete() {
        return this.canBeDelete;
    }

    /**
     * 设置是否可删除:0.否、1.是
     *
     * @param canBeDelete 是否可删除:0.否、1.是
     */
    public void setCanBeDelete(Integer canBeDelete) {
        this.canBeDelete = canBeDelete;
    }

    /**
     * 获取是否可修改:0.否、1.是
     *
     * @return Integer
     */
    public Integer getCanBeModify() {
        return this.canBeModify;
    }

    /**
     * 设置是否可修改:0.否、1.是
     *
     * @param canBeModify 是否可修改:0.否、1.是
     */
    public void setCanBeModify(Integer canBeModify) {
        this.canBeModify = canBeModify;
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
        if (Objects.nonNull(this.name) && !"".equals(this.name)) {
            filter.put("nameEq", getName());
        }
        return filter;
    }

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Tree<Integer, Group>> children = new ArrayList<>();

    @Override
    public List<Tree<Integer, Group>> getChildren() {
        return this.children;
    }

    /**
     * 设置子节点
     *
     * @param children 子节点
     */
    public void setChildren(List<Tree<Integer, Group>> children) {
        this.children = children;
    }

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, Group> parent) {
        return false;
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
    public String getCanBeDeleteName() {
        return NameValueEnumUtils.getName(canBeDelete, YesOrNo.class);
    }

    /**
     * 获取是否可修改名称
     *
     * @return String
     */
    public String getCanBeModifyName() {
        return NameValueEnumUtils.getName(canBeModify, YesOrNo.class);
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