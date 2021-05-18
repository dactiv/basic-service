package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.tree.Tree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 数据字典
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("dataDictionary")
@TableName("tb_data_dictionary")
public class DataDictionary implements Tree<Integer, DataDictionary>, Serializable {

    private static final long serialVersionUID = 4219144269288469584L;
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

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //


    /**
     * 子类节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, DataDictionary>> children = new LinkedList<>();

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(status, DisabledOrEnabled.class);
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

