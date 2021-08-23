package com.github.dactiv.basic.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * <p>字典类型实体</p>
 * <p>Table: tb_dictionary_type - 字典类型</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("dictionaryType")
@TableName("tb_dictionary_type")
public class DictionaryType implements Tree<Integer, DictionaryType>, NumberIdEntity<Integer> {

    private static final long serialVersionUID = 2211302874891670273L;
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

    @TableField(exist = false)
    private List<Tree<Integer, DictionaryType>> children = new LinkedList<>();

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


