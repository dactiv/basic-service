package com.github.dactiv.basic.socket.server.domain.enitty;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;


/**
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @author maurice
 * @since 2021-10-08 10:36:59
 */
@Data
@NoArgsConstructor
@Alias("room")
@TableName("tb_room")
public class Room implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = -8032662822919772839L;

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
     * 类型
     */
    private String type = DefaultUserDetailsService.DEFAULT_TYPES;

    /**
     * 备注
     */
    private String remark;

}