package com.github.dactiv.basic.authentication.domain.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.*;

import org.apache.ibatis.type.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * <p>Table: tb_department - 部门表</p>
 *
 * @author maurice.chen
 *
 * @since 2022-02-09 06:47:53
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Alias("department")
@TableName("tb_department")
public class DepartmentEntity implements Serializable {

    private static final long serialVersionUID = 6607927907369680571L;

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
    private String name;

    /**
     * 父类 ID
     */
    private Integer parentId;

    /**
     * 人员总数
     */
    private Integer count;

    /**
     * 备注
     */
    private String remark;

}