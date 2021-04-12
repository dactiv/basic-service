package com.fuyu.basic.config.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.tree.Tree;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * 字典类型实体
 *
 * @author maurice.chen
 */
@Alias("dictionaryType")
public class DictionaryType extends IntegerIdEntity implements Tree<Integer, DictionaryType> {

    private static final long serialVersionUID = 1L;

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 128)
    private String code;

    /**
     * 类型名称
     */
    @NotEmpty
    @Length(max = 64)
    private String name;

    /**
     * 父字典类型,根节点为 null
     */
    private Integer parentId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据字典类型实体类
     */
    public DictionaryType() {
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
     * 获取类型名称
     *
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置类型名称
     *
     * @param name 类型名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取父字典类型,根节点为 null
     *
     * @return Integer
     */
    public Integer getParentId() {
        return this.parentId;
    }

    /**
     * 设置父字典类型,根节点为 null
     *
     * @param parentId 父字典类型,根节点为 null
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Tree<Integer, DictionaryType>> children = new ArrayList<>(16);

    @Override
    public List<Tree<Integer, DictionaryType>> getChildren() {
        return children;
    }

    /**
     * 设置子节点
     *
     * @param children 子节点集合
     */
    public void setChildren(List<Tree<Integer, DictionaryType>> children) {
        this.children = children;
    }

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

    @Override
    public boolean isChildren(Tree<Integer, DictionaryType> parent) {
        DictionaryType parentEntity = Casts.cast(parent);
        return Objects.equals(parentEntity.getId(), this.parentId);
    }
}


