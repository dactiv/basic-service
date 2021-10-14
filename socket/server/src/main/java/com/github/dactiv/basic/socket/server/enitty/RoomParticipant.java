package com.github.dactiv.basic.socket.server.enitty;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;


/**
 * <p>Table: tb_room_participant - 房间参与者，用于说明某个房间里存在些什么人</p>
 *
 * @author maurice
 *
 * @since 2021-10-08 10:36:59
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Alias("roomParticipant")
@TableName("tb_room_participant")
public class RoomParticipant implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 3092205314058668475L;
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
     * 用户 id
     */
    private Integer userId;

    /**
     * 房间 id
     */
    private Integer roomId;

}