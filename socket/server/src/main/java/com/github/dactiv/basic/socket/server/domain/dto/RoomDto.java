package com.github.dactiv.basic.socket.server.domain.dto;

import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 房间 dto，用于房间关联房间参与者实体集合合并的的对象。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoomDto extends RoomEntity {

    private static final long serialVersionUID = -3988898426840609747L;
    /**
     * 参与者集合
     */
    private List<RoomParticipantEntity> participantList = new LinkedList<>();
}
