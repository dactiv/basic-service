package com.github.dactiv.basic.config.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.IntegerIdEntity;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.tree.Tree;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 数据字典
 *
 * @author maurice.chen
 */
@Alias("dataDictionary")
public class DataDictionary extends IntegerIdEntity implements Tree<Integer, DataDictionary> {

    private static final long serialVersionUID = 1L;

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 256)
    private String code;

    /**
     * 名称
     */
    @NotEmpty
    @Length(max = 64)
    private String name;

    /**
     * 值
     */
    @NotEmpty
    private String value;

    /**
     * 状态:0.禁用,1.启用
     */
    @NotNull
    @Range(min = 0, max = 1)
    private Integer status;

    /**
     * 对应字典类型
     */
    private Integer typeId;

    /**
     * 根节点为 null
     */
    private Integer parentId;

    /**
     * 顺序值
     */
    @Max(999)
    private Integer sort;

    /**
     * 等级
     */
    private String level;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据字典实体类
     */
    public DataDictionary() {
    }

    /**
     * 获取键名称
     *
     * @return String
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置键名称
     *
     * @param code 键名称
     */
    public void setCode(String code) {
        this.code = code;
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
     * 获取等级
     *
     * @return 等级
     */
    public String getLevel() {
        return level;
    }

    /**
     * 设置等级
     *
     * @param level 等级
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 获取值
     *
     * @return String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取状态:0.禁用,1.启用
     *
     * @return Integer
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 设置状态:0.禁用,1.启用
     *
     * @param status 状态:0.禁用,1.启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取对应字典类型
     *
     * @return Integer
     */
    public Integer getTypeId() {
        return this.typeId;
    }

    /**
     * 设置对应字典类型
     *
     * @param typeId 对应字典类型
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    /**
     * 获取根节点为 null
     *
     * @return Integer
     */
    public Integer getParentId() {
        return this.parentId;
    }

    /**
     * 设置根节点为 null
     *
     * @param parentId 根节点为 null
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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
        return filter;
    }

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //


    /**
     * 子类节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Tree<Integer, DataDictionary>> children = new ArrayList<>(16);

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(status, DisabledOrEnabled.class);
    }

    @Override
    public List<Tree<Integer, DataDictionary>> getChildren() {
        return children;
    }

    /**
     * 设置子节点
     *
     * @param children 子节点集合
     */
    public void setChildren(List<Tree<Integer, DataDictionary>> children) {
        this.children = children;
    }

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

    @Override
    public boolean isChildren(Tree<Integer, DataDictionary> parent) {
        DataDictionary parentEntity = Casts.cast(parent);
        return Objects.equals(parentEntity.getId(), this.parentId);
    }
}

