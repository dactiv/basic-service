package com.github.dactiv.basic.socket.server.controller.room;

import com.github.dactiv.basic.socket.server.enitty.Room;
import com.github.dactiv.basic.socket.server.enitty.RoomParticipant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 房间响应实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoomResponseBody extends Room {

    private static final long serialVersionUID = -3988898426840609747L;
    /**
     * 参与者集合
     */
    private List<RoomParticipant> participantList = new LinkedList<>();
}
