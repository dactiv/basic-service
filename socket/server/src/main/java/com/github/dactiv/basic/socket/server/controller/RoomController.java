package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.domain.body.response.RoomResponseBody;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.service.RoomService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @param room 房间信息
     * @param userIds 参与者用户 id
     *
     * @return 新的房间主键 id
     */
    @PostMapping("createRoom")
    @PreAuthorize("isFullyAuthenticated()")
    @SocketMessage(Constants.WEB_FILTER_RESULT_ID)
    @Plugin(name = "创建房间", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public RestResult<?> createRoom(@CurrentSecurityContext SecurityContext securityContext,
                                    RoomEntity room,
                                    @RequestParam List<Integer> userIds) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        RoomResponseBody body = roomService.create(room, userIds, userId);

        SocketResultHolder.get().addBroadcastSocketMessage(room.getName(), RoomEntity.ROOM_CREATE_EVENT_NAME, body);
        return RestResult.ofSuccess("创建" + room.getId() + "房间成功", room.getId());
    }

    /**
     * 获取当前用户的房间集合
     *
     * @param securityContext 安全上下文
     *
     * @return 房间响应实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getCurrentPrincipalRooms")
    @Plugin(name = "获取当前用户房间集合", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public List<RoomResponseBody> getCurrentPrincipalRooms(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        return roomService.findResponseBodiesByUserid(userId);
    }
}
