package com.github.dactiv.basic.config.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.config.domain.meta.DataDictionaryMeta;
import com.github.dactiv.basic.config.enumerate.ValueTypeEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * <p>数据字典实体类</p>
 * <p>Table: tb_data_dictionary - 数据字典</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("dataDictionary")
@TableName("tb_data_dictionary")
@EqualsAndHashCode(callSuper = true)
public class DataDictionaryEntity extends DataDictionaryMeta implements Tree<Integer, DataDictionaryEntity>, NumberIdEntity<Integer> {

    private static final long serialVersionUID = 4219144269288469584L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 256)
    private String code;

    /**
     * 是否启用:0.禁用,1.启用
     */
    @NotNull
    private DisabledOrEnabled enabled;

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
     * 备注
     */
    private String remark;

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //


    /**
     * 子类节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, DataDictionaryEntity>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

    @Override
    public boolean isChildren(Tree<Integer, DataDictionaryEntity> parent) {
        DataDictionaryEntity parentEntity = Casts.cast(parent);
        return Objects.equals(parentEntity.getId(), this.parentId);
    }
}

