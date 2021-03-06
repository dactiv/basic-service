package com.github.dactiv.basic.socket.server.domain.enitty;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.enumerate.RoomParticipantRoleEnum;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.util.Date;


/**
 * <p>Table: tb_room_participant - 房间参与者，用于说明某个房间里存在些什么人</p>
 *
 * @author maurice
 * @since 2021-10-08 10:36:59
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Alias("roomParticipant")
@TableName("tb_room_participant")
@RequiredArgsConstructor(staticName = "of")
public class RoomParticipantEntity implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 3092205314058668475L;

    public static final String USER_ID_FIELD_NAME = "userId";

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
     * 最后发送消息的时间
     */
    private Date lastSendTime;

    /**
     * 用户 id
     */
    @NonNull
    private Integer userId;

    /**
     * 角色
     */
    @NonNull
    private RoomParticipantRoleEnum role;

    /**
     * 房间 id
     */
    @NonNull
    @Exclude(SystemConstants.WEB_FILTER_RESULT_ID)
    private Integer roomId;

}