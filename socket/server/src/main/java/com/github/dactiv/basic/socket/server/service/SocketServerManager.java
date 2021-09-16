package com.github.dactiv.basic.socket.server.service;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.github.dactiv.basic.commons.feign.authentication.AuthenticationService;
import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.entity.SocketMessage;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.entity.SocketUserMessage;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.server.service.message.MessageSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.DeviceIdProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.redisson.api.RBucket;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * socket 服务管理
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class SocketServerManager implements CommandLineRunner, DisposableBean,
        AuthorizationListener, ConnectListener, DisconnectListener {

    /**
     * 默认 socket 链接头的设备唯一识别名称
     */
    public static final String DEFAULT_IDENTIFIED_HEADER_NAME = "io";
    /**
     * 当 socket 链接时的通知事件
     */
    public static final String CONNECTED_EVENT_NAME = "SOCKET_CONNECTED_EVENT";
    /**
     * 当 socket 服务主动断开时的通知事件
     */
    public static final String SERVER_DISCONNECTED_EVENT_NAME = "SOCKET_SERVER_DISCONNECT_EVENT";

    /**
     * 当 socket 服务要求客户端断开时的通知事件
     */
    public static final String CLIENT_DISCONNECTED_EVENT_NAME = "SOCKET_CLIENT_DISCONNECT_EVENT";

    @Autowired
    private DeviceIdContextRepository securityContextRepository;

    @Autowired
    private RemoteApiService remoteApiService;

    @Autowired
    private SocketClientTemplate socketClientTemplate;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private List<MessageSender> messageSenderList;

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private NacosServiceManager nacosServiceManager;

    /**
     * 发送消息
     *
     * @param message socket 消息
     */
    public void sendMessage(SocketMessage<?> message) {
        messageSenderList.stream().filter(m -> m.isSupport(message)).forEach(m -> m.sendMessage(message, socketServer));
    }

    /**
     * 加入聊天房间频道
     *
     * @param userDetails socket 用户明细
     */
    public void joinRoom(SocketUserDetails userDetails, List<String> rooms) {

        SocketIOClient client = socketServer.getClient(UUID.fromString(userDetails.getDeviceIdentified()));

        if (Objects.nonNull(client)) {

            client.getAllRooms().forEach(client::leaveRoom);

            rooms.forEach(client::joinRoom);
        }

    }

    @Override
    public void run(String... args) throws NacosException {

        socketServer.start();

        log.info("已启动 socket 服务，端口为:" + socketServer.getConfiguration().getPort());

        NamingService naming = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());

        naming.registerInstance(
                remoteApiService.getSocketServerProperties().getServerName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                remoteApiService.getSocketServerProperties().getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "注册链接 socket 服务 {} 到 nacos，端口为: {}",
                remoteApiService.getSocketServerProperties().getServerName(),
                socketServer.getConfiguration().getPort()
        );
    }

    @Override
    public void destroy() throws NacosException {

        NamingService naming = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());

        naming.deregisterInstance(
                remoteApiService.getSocketServerProperties().getServerName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                remoteApiService.getSocketServerProperties().getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "从 nacos 关闭 链接 socket 服务 {}，端口为: {}",
                remoteApiService.getSocketServerProperties().getServerName(),
                socketServer.getConfiguration().getPort()
        );

        socketServer.stop();

        log.info("已关闭 socket 服务");
    }

    /**
     * 当发起链接时，进入此方法进行用户信息认证
     *
     * @param data socket 链接信息
     *
     * @return 成功返回 true，否则 false
     */
    @Override
    public boolean isAuthorized(HandshakeData data) {

        String username = data.getSingleUrlParam(RequestAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);

        String password = data.getSingleUrlParam(RequestAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY);

        String deviceIdentified = data.getHttpHeaders().get(DEFAULT_IDENTIFIED_HEADER_NAME);

        String userId = data.getHttpHeaders().get(DeviceIdProperties.DEFAULT_USER_ID_HEADER_NAME);

        try {

            ResponseEntity<Map<String, Object>> result = remoteApiService.validMobileUserDetails(
                    username,
                    password,
                    userId,
                    deviceIdentified
            );

            if (!HttpStatus.OK.equals(result.getStatusCode())) {
                return false;
            }

            if (Objects.isNull(result.getBody())) {
                return false;
            }

            Object resultData = result.getBody().get(RestResult.DEFAULT_DATA_NAME);

            if (Objects.isNull(resultData)) {
                return false;
            }

            Map<String, Object> map = Casts.cast(resultData);

            Map<String, Object> userResult = authenticationService.getConsoleUser(Casts.cast(userId, Integer.class));

            SocketUserDetails user = new SocketUserDetails();

            if (MapUtils.isNotEmpty(userResult)) {

                user = Casts.convertValue(userResult, SocketUserDetails.class);

                if(!UserStatus.Enabled.getValue().equals(userResult.get(RestResult.DEFAULT_STATUS_NAME))) {
                    log.warn("ID 为 [" + userId + "] 的用户非启用");
                    return false;
                }

            }

            List<Map<String, Object>> roleAuthorities = Casts.cast(
                    map.get(SecurityUserDetails.DEFAULT_ROLE_AUTHORITIES_FIELD_NAME)
            );

            // 设置用户权限
            user.setRoleAuthorities(
                    roleAuthorities.stream().map(RoleAuthority::new).collect(Collectors.toList())
            );

            RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);

            SecurityContext securityContext = bucket.get();

            if (Objects.nonNull(securityContext)) {

                Authentication authentication = securityContext.getAuthentication();

                SocketUserDetails details = Casts.cast(authentication.getDetails());

                if (Objects.nonNull(details) && ConnectStatus.Connect.getValue().equals(details.getConnectStatus())) {
                    disconnect(details);
                }
            }

            // 设置认证系统的用户 id
            user.setUserId(Casts.cast(userId, Integer.class));

            // 设置登陆账户和密码以及状态
            user.setUsername(username);
            user.setPassword(password);
            user.setConnectStatus(ConnectStatus.Connecting.getValue());

            // 设置 socket server 的 id 地址和端口
            user.setSocketServerIp(discoveryProperties.getIp());
            user.setPort(remoteApiService.getSocketServerProperties().getPort());
            user.setServerId(remoteApiService.getSocketServerProperties().getId());

            // 设置当前设备 id
            user.setDeviceIdentified(deviceIdentified);

            // 刷新一下缓存信息
            refreshSpringSecurityContext(user);

            return true;

        } catch (Exception e) {
            log.error("socket 链接授权失败", e);
            return false;
        }

    }

    /**
     * 刷新 spring 安全上下文
     *
     * @param user socket 用户明细
     */
    public void refreshSpringSecurityContext(SocketUserDetails user) {

        Objects.requireNonNull(user.getDeviceIdentified(), "设备 id 识别不能为空");

        String deviceIdentified = user.getDeviceIdentified();

        RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);

        SecurityContext securityContext = bucket.get();

        PrincipalAuthenticationToken token;

        if (securityContext != null && PrincipalAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())) {
            token = Casts.cast(securityContext.getAuthentication());
        } else {

            token = new PrincipalAuthenticationToken(
                    new UsernamePasswordAuthenticationToken(user.getDeviceIdentified(), null),
                    SocketUserDetails.DEFAULT_TYPE,
                    user.getAuthorities()
            );

        }

        token.setAuthenticated(true);
        token.setDetails(user);

        SecurityContext newSecurityContext = new SecurityContextImpl(token);

        Optional<HttpServletRequest> requestOptional = SpringMvcUtils.getHttpServletRequest();
        Optional<HttpServletResponse> responseOptional = SpringMvcUtils.getHttpServletResponse();

        if (requestOptional.isPresent() && responseOptional.isPresent()) {

            HttpServletRequest request = requestOptional.get();

            String requestDeviceIdentified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

            if (requestDeviceIdentified.equals(deviceIdentified)) {
                securityContextRepository.saveContext(newSecurityContext, requestOptional.get(), responseOptional.get());
            } else {
                bucket.set(newSecurityContext);
            }

        } else {
            bucket.set(newSecurityContext);
        }

        if (log.isDebugEnabled()) {
            log.debug("刷新 ID 为 [" + user.getId() + "] 的用户缓存");
        }
    }

    /**
     * 断开链接
     *
     * @param details socket 用户明细实现
     */
    private void disconnect(SocketUserDetails details) {
        try {

            SocketUserMessage<?> socketUserDetails = SocketUserMessage.of(
                    details,
                    CLIENT_DISCONNECTED_EVENT_NAME,
                    RestResult.ofSuccess("您的账号已在其他客户端登陆，如果非本人操作，请及时修改密码")
            );

            Map<String, Object> result = socketClientTemplate.unicast(socketUserDetails);

            if (log.isDebugEnabled()) {
                log.debug(
                        "ID 为 [{}] 的用户在其他设备登录，断开上一次设备的链接，响应结果为:{}",
                        details.getId(),
                        result
                );
            }
        } catch (Exception e) {
            log.error("登出用户失败", e);
        }
    }

    /**
     * 当客户端链接成功时，进入此方法
     *
     * @param client 客户端信息
     */
    @Override
    public void onConnect(SocketIOClient client) {

        String deviceIdentified = client.getHandshakeData().getHttpHeaders().get(DEFAULT_IDENTIFIED_HEADER_NAME);

        SecurityContext securityContext = securityContextRepository.getSecurityContextBucket(deviceIdentified).get();

        if (Objects.isNull(securityContext)) {
            log.warn("在认证通过后连接 socket 时出现获取用户信息为 null 的情况, deviceIdentified 为:" + deviceIdentified);
            client.sendEvent(SERVER_DISCONNECTED_EVENT_NAME, Casts.writeValueAsString(RestResult.of("找不到当前认证的用户")));
            client.disconnect();
        }

        SocketUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

        try {

            // 检查服务器是否爆满状态。如果没有抛出异常，表示没问题
            ResponseEntity<Map<String, Object>> entity = remoteApiService.onUserConnect(deviceIdentified, user.getUserId());

            if (!HttpStatus.OK.equals(entity.getStatusCode())) {

                String msg = Objects
                        .requireNonNull(entity.getBody())
                        .get(RestResult.DEFAULT_MESSAGE_NAME)
                        .toString();

                throw new SystemException(msg);
            }

            // 如果不是新用户，同步 account 应用的用户数据
            if (!user.isNew()) {

                Integer userId = Casts.cast(user.getId(), Integer.class);

                //userServiceFeignClient.onConnect(user.getDeviceIdentified(), userId);

                /*List<Map<String, Object>> rooms = userRoomServiceFeignClient.getCurrentUserRoom(
                        user.getDeviceIdentified(),
                        userId,
                        false
                );
                if (CollectionUtils.isNotEmpty(rooms)) {
                    rooms.stream().map(s -> s.get("code").toString()).forEach(client::joinRoom);
                }*/

            }

        } catch (Exception e) {
            log.error("调用 onUserConnect api 出错", e);
            client.sendEvent(SERVER_DISCONNECTED_EVENT_NAME, Casts.writeValueAsString(RestResult.of("服务异常，请稍后再试")));
            client.disconnect();
            return;
        }

        // 设置链接状态为已连接
        user.setConnectStatus(ConnectStatus.Connect.getValue());
        // 设置最后链接时间
        user.setConnectTime(new Date());

        refreshSpringSecurityContext(user);

        client.sendEvent(CONNECTED_EVENT_NAME, Casts.writeValueAsString(RestResult.ofSuccess(user)));

        log.info("设备: " + deviceIdentified + "建立连接成功, IP 为: " + client.getRemoteAddress().toString() + (Objects.nonNull(user.getId()) ? ", 用户为: " + user.getId() : ""));

    }

    @Override
    public void onDisconnect(SocketIOClient client) {

        String uuid = client.getSessionId().toString();

        String deviceIdentified = client.getHandshakeData().getHttpHeaders().get(DEFAULT_IDENTIFIED_HEADER_NAME);
        RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);

        SecurityContext securityContext = bucket.get();

        if (Objects.nonNull(securityContext)) {

            SocketUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

            try {

                // 检查服务器是否爆满状态。如果没有抛出异常，表示没问题
                ResponseEntity<Map<String, Object>> entity = remoteApiService.onUserDisconnect(deviceIdentified, user.getUserId());

                if (!HttpStatus.OK.equals(entity.getStatusCode())) {

                    String msg = Objects
                            .requireNonNull(entity.getBody())
                            .get(RestResult.DEFAULT_MESSAGE_NAME)
                            .toString();

                    throw new SystemException(msg);
                }

                if (!ConnectStatus.Connecting.getValue().equals(user.getConnectStatus())) {

                    if (log.isDebugEnabled()) {
                        log.debug("ID 为 [" + user.getId() + "] 的用户已断开链接,设置断开信息并保存");
                    }

                    // 如果不是新用户，同步 account 应用的用户数据
                    if (!user.isNew()) {

                        user.setDeviceIdentified(null);
                        user.setSocketServerIp(null);
                        user.setConnectStatus(ConnectStatus.Disconnected.getValue());
                    }

                }

                // 删除 spring security 用户信息
                bucket.deleteAsync();

            } catch (Exception e) {
                log.error("调用 onUserConnect api 出错", e);
            }

            log.info("IP: {} UUID: {} 设备断开连接 {}" ,
                    client.getRemoteAddress().toString(),
                    uuid,
                    (Objects.nonNull(user) ? ", 用户为: " + (Objects.nonNull(user.getId()) ? user.getUsername() : "新创建的用户") : "")
            );

        }
    }
}
