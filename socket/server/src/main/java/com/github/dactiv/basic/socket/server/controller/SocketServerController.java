package com.github.dactiv.basic.socket.server.controller;

import com.github.dactiv.basic.socket.client.entity.BroadcastMessage;
import com.github.dactiv.basic.socket.client.entity.MultipleUnicastMessage;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.entity.UnicastMessage;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * socket 服务控制器
 *
 * @author maurice.chen
 */
@RestController
public class SocketServerController {

    @Autowired
    private SocketServerManager socketServerManager;

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
     * @param securityContext spring 安全上下文
     * @param rooms           房间集合
     *
     * @return rest 结果集
     */
    @PostMapping("joinRoom")
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "加入房间频道", sources = "SocketUser")
    public RestResult<?> joinRoom(@CurrentSecurityContext SecurityContext securityContext,
                                  @RequestParam List<String> rooms) {

        SocketUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        socketServerManager.joinRoom(userDetails, rooms);

        return RestResult.of("加入[" + rooms + "]房间成功");
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
                .peek(r -> socketServerManager.sendMessage(r))
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
                .forEach(r -> socketServerManager.sendMessage(r));

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
                .peek(r -> socketServerManager.sendMessage(r))
                .map(r -> RestResult.ofSuccess("单播 socket 成功", r.getMessage().getData()))
                .collect(Collectors.toList());
    }

}
