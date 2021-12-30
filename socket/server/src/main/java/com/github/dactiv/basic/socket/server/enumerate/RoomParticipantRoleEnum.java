package com.github.dactiv.basic.socket.server.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房间参与者角色
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RoomParticipantRoleEnum implements NameValueEnum<Integer> {

    /**
     * 参与者
     */
    PARTICIPANT(10, "参与者"),
    /**
     * 拥有者
     */
    OWNER(20, "拥有者");

    /**
     * 值
     */
    private Integer value;

    /**
     * 名称
     */
    private String name;

}
