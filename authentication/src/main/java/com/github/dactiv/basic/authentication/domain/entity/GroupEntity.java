package com.github.dactiv.basic.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.handler.NameValueEnumTypeHandler;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.github.dactiv.basic.commons.Constants.SOCKET_RESULT_ID;

/**
 * <p>用户组实体类</p>
 * <p>Table: tb_group - 用户组表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("group")
@NoArgsConstructor
@EqualsAndHashCode
@TableName(value = "tb_group", autoResultMap = true)
@IncludeView(value = SOCKET_RESULT_ID, properties = {"id", "name", "parentId"})
public class GroupEntity implements Tree<Integer, GroupEntity>, NumberIdEntity<Integer> {

    private static final long serialVersionUID = 5357157352791368716L;

    /**
     * 创建 socket 事件名称
     */
    public static final String CREATE_SOCKET_EVENT_NAME = "create_group";

    /**
     * 更新 socket 事件名称
     */
    public static final String UPDATE_SOCKET_EVENT_NAME = "update_group";
    /**
     * 删除 socket 事件名称
     */
    public static final String DELETE_SOCKET_EVENT_NAME = "delete_group";

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
     * 来源
     *
     * @see Plugin#sources()
     */
    @NotEmpty
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ResourceSourceEnum> sources = new LinkedList<>();

    /**
     * 父类 id
     */
    private Integer parentId;

    /**
     * 是否可删除:0.否、1.是
     */
    @NotNull
    @Range(min = 0, max = 1)
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private YesOrNo removable;

    /**
     * 是否可修改:0.否、1.是
     */
    @NotNull
    @Range(min = 0, max = 1)
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private YesOrNo modifiable;

    /**
     * 状态:0.禁用、1.启用
     */
    @NotNull
    @Range(min = 0, max = 1)
    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private DisabledOrEnabled status;

    /**
     * 资源 id 集合
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, List<String>> resourceMap = new LinkedHashMap<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, GroupEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return getParentId();
    }

    @Override
    public boolean isChildren(Tree<Integer, GroupEntity> parent) {
        GroupEntity group = Casts.cast(parent);
        return Objects.equals(group.getId(), this.getParent());
    }

}