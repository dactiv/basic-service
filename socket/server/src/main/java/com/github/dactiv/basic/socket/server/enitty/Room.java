package com.github.dactiv.basic.socket.server.enitty;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;


/**
 * <p>Table: tb_room - 房间信息，用于说明当前用户存在些什么房间。</p>
 *
 * @author maurice
 *
 * @since 2021-10-08 10:36:59
 */
@Data
@NoArgsConstructor
@Alias("room")
@TableName("tb_room")
public class Room {

    private static final long serialVersionUID = -8032662822919772839L;

    /**
    * 主键
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

}