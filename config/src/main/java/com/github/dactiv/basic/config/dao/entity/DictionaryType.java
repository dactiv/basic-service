package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.commons.tree.Tree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 字典类型实体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("dictionaryType")
@TableName("tb_dictionary_type")
public class DictionaryType implements Tree<Integer, DictionaryType>, Serializable {

    private static final long serialVersionUID = -2211302874891670273L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 版本号
     */
    @Version
    @JsonIgnore
    private Integer updateVersion = 1;

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


