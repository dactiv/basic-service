package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.basic.socket.server.controller.room.RoomResponseBody;
import com.github.dactiv.basic.socket.server.enitty.Room;
import com.github.dactiv.basic.socket.server.service.room.RoomService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
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
        sources = ResourceSource.CONSOLE_SOURCE_VALUE
)
public class RoomController {

    @Autowired
    private RoomService roomService;

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
    @Plugin(name = "创建房间", sources = ResourceSource.SOCKET_USER_SOURCE_VALUE)
    public RestResult<?> createRoom(@CurrentSecurityContext SecurityContext securityContext,
                                    Room room,
                                    @RequestParam List<Integer> userIds) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        roomService.createRoom(room, userIds, userId);

        return RestResult.ofSuccess("创建" + room.getId() + "房间成功", room.getId());
    }

    /**
     * 获取当前用户的房间集合
     *
     * @param securityContext 安全上下文
     *
     * @return 房间响应实体集合
     */
    @GetMapping("getCurrentPrincipalRooms")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取当前用户房间集合", sources = ResourceSource.SOCKET_USER_SOURCE_VALUE)
    public List<RoomResponseBody> getCurrentPrincipalRooms(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        return roomService.findRoomResponseBodyList(userId);
    }
}
