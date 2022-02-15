package com.github.dactiv.basic.socket.server.controller;

import ch.qos.logback.core.joran.action.Action;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.dto.RoomDto;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.basic.socket.server.service.RoomService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 房间控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("room")
@Plugin(
        name = "房间管理",
        id = "chat",
        parent = "socket-server",
        icon = "icon-chat-room",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 创建房间
     *
     * @param securityContext 安全上下文
     * @param room            房间信息
     * @param userIds         参与者用户 id
     *
     * @return rest 结果集
     */
    @PostMapping("createRoom")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "创建房间", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE, audit = true)
    public RestResult<?> createRoom(@CurrentSecurityContext SecurityContext securityContext,
                                    RoomEntity room,
                                    @RequestParam List<Integer> userIds) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        roomService.create(room, userIds, userId);

        return RestResult.of("创建" + room.getName() + "房间成功");
    }

    /**
     * 修改房间名称
     *
     * @param securityContext 安全上下文
     * @param name            新名称
     * @param id              房间 id
     *
     * @return rest 结果集
     */
    @PostMapping("renameRoom")
    @PreAuthorize("isFullyAuthenticated()")
    @SocketMessage(SystemConstants.CHAT_FILTER_RESULT_ID)
    @Plugin(name = "修改房间名称", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE, audit = true)
    public RestResult<?> renameRoom(@CurrentSecurityContext SecurityContext securityContext,
                                    @RequestParam String name,
                                    @RequestParam Integer id) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        boolean socket = roomService.rename(userId, name, id);

        if (socket) {
            Map<String, Object> message = Map.of(
                    IdEntity.ID_FIELD_NAME, id,
                    Action.NAME_ATTRIBUTE, name,
                    RoomParticipantEntity.USER_ID_FIELD_NAME, userId
            );
            SocketResultHolder
                    .get()
                    .addBroadcastSocketMessage(
                            id.toString(),
                            RoomEntity.ROOM_RENAME_EVENT_NAME,
                            message
                    );
        }

        return RestResult.of("修改名称成功");
    }

    /**
     * 退出/解散房间
     *
     * @param securityContext 安全上下文
     * @param id              房间信息 id
     *
     * @return rest 结果集
     */
    @PostMapping("exitRoom")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "创建房间", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE, audit = true)
    public RestResult<?> exitRoom(@CurrentSecurityContext SecurityContext securityContext, @RequestParam Integer id) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        boolean dissolution = roomService.exitRoom(userId, id);

        return RestResult.ofSuccess((dissolution ? "解散" : "退出") + "成功", id);
    }

    /**
     * 获取当前用户的房间集合
     *
     * @param securityContext 安全上下文
     *
     * @return 房间响应实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("getCurrentPrincipalRooms")
    @Plugin(name = "获取当前用户房间集合", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public List<RoomDto> getCurrentPrincipalRooms(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        return roomService.findByUserId(userId);
    }

    /**
     * 获取房间实体集合信息
     *
     * @param ids 房间 id 集合
     *
     * @return 房间实体集合
     */
    @PostMapping("getRooms")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取当前用户房间集合", sources = ResourceSourceEnum.SYSTEM_SOURCE_VALUE)
    public List<RoomDto> getRooms(@RequestParam List<Integer> ids) {
        return roomService.getRomDto(ids);
    }
}
