package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.socket.client.domain.BroadcastMessage;
import com.github.dactiv.basic.socket.client.domain.MultipleUnicastMessage;
import com.github.dactiv.basic.socket.client.domain.UnicastMessage;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * socket 服务控制器
 *
 * @author maurice.chen
 */
@RestController
public class SocketServerController {

    private final SocketServerManager socketServerManager;

    public SocketServerController(SocketServerManager socketServerManager) {
        this.socketServerManager = socketServerManager;
    }

    /**
     * 获取健康检查
     *
     * @return 健康检查
     */
    @GetMapping("health")
    public Health health() {
        return Health.up().build();
    }

    /**
     * 加入房间频道
     *
     * @param deviceIdentifies 用户唯一识别集合
     * @param rooms            房间集合
     *
     * @return rest 结果集
     */
    @PostMapping("joinRoom")
    @PreAuthorize("hasRole('BASIC') || hasRole('ORDINARY')")
    public RestResult<?> joinRoom(@RequestParam List<String> deviceIdentifies,
                                  @RequestParam List<String> rooms) {

        deviceIdentifies
                .stream()
                .map(socketServerManager::getSocketUserDetails)
                .filter(Objects::nonNull)
                .forEach(u -> socketServerManager.joinRoom(u, rooms));

        return RestResult.of("加入 " + rooms + " 房间成功");
    }

    /**
     * 离开房间频道
     *
     * @param deviceIdentifies 用户唯一识别集合
     * @param rooms            房间集合
     *
     * @return rest 结果集
     */
    @PostMapping("leaveRoom")
    @PreAuthorize("hasRole('BASIC') || hasRole('ORDINARY')")
    public RestResult<?> leaveRoom(@RequestParam List<String> deviceIdentifies,
                                   @RequestParam List<String> rooms) {

        deviceIdentifies
                .stream()
                .map(socketServerManager::getSocketUserDetails)
                .filter(Objects::nonNull)
                .forEach(u -> socketServerManager.leaveRoom(u, rooms));

        return RestResult.of("离开 " + rooms + " 房间成功");
    }

    /**
     * 广播 socket 信息
     *
     * @param messageList socket 消息集合
     *
     * @return 消息结果集
     */
    @PostMapping("broadcast")
    @PreAuthorize("hasRole('BASIC') || hasRole('ORDINARY')")
    public List<RestResult<?>> broadcast(@RequestBody List<BroadcastMessage<?>> messageList) {
        return messageList
                .stream()
                .peek(socketServerManager::sendMessage)
                .map(r -> RestResult.ofSuccess("广播 socket 成功", r.getMessage().getData()))
                .collect(Collectors.toList());


    }

    /**
     * 单播多数据 socket 信息
     *
     * @param messageList socket 消息集合
     *
     * @return 消息结果集
     */
    @PostMapping("multipleUnicast")
    @PreAuthorize("hasRole('BASIC') || hasRole('ORDINARY')")
    public List<RestResult<?>> multipleUnicast(@RequestBody List<MultipleUnicastMessage<?>> messageList) {

        messageList
                .stream()
                .flatMap(r -> r.toUnicastMessageList().stream())
                .forEach(socketServerManager::sendMessage);

        return messageList
                .stream()
                .map(r -> RestResult.ofSuccess("单播 socket 成功", r.getMessage().getData()))
                .collect(Collectors.toList());
    }

    /**
     * 单播 socket 信息
     *
     * @param messageList socket 消息集合
     *
     * @return 消息结果集
     */
    @PostMapping("unicast")
    @PreAuthorize("hasRole('BASIC') || hasRole('ORDINARY')")
    public List<RestResult<?>> unicast(@RequestBody List<UnicastMessage<?>> messageList) {

        return messageList
                .stream()
                .peek(socketServerManager::sendMessage)
                .map(r -> RestResult.ofSuccess("单播 socket 成功", r.getMessage().getData()))
                .collect(Collectors.toList());
    }

    /**
     * 获取临时消息
     *
     * @param securityContext 安全上下文
     * @param types           消息类型集合
     *
     * @return 临时消息 map
     */
    @PostMapping("getTempMessageMap")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "获取临时消息", parent = "socket-server", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public Map<String, List<Object>> getTempMessageMap(@CurrentSecurityContext SecurityContext securityContext,
                                                       @RequestParam List<String> types) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        Map<String, List<Object>> result = new LinkedHashMap<>();

        for (String type : types) {
            List<Object> objects = socketServerManager.getTempMessages(userId, type);

            if (CollectionUtils.isEmpty(objects)) {
                continue;
            }
            result.put(type, objects);
        }

        return result;
    }

    /**
     * 清除临时消息
     *
     * @param securityContext 安全上下文
     * @param types           消息类型集合
     *
     * @return rest 结果集
     */
    @PostMapping("clearTempMessage")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "清除临时消息", parent = "socket-server", sources = ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE)
    public RestResult<?> clearTempMessage(@CurrentSecurityContext SecurityContext securityContext,
                                          @RequestParam List<String> types) throws Exception {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());

        for (String type : types) {
            socketServerManager.clearTempMessage(userId, type);
        }

        return RestResult.of("清除临时消息成功");
    }

}
