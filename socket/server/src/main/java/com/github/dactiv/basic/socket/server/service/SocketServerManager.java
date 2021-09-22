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
import com.github.dactiv.basic.socket.client.entity.BroadcastMessage;
import com.github.dactiv.basic.socket.client.entity.SocketMessage;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.entity.SocketUserMessage;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.server.config.SocketServerProperties;
import com.github.dactiv.basic.socket.server.service.message.MessageSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.config.DeviceIdProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.ResourceAuthority;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

    private static final String DEFAULT_RESOURCE_AUTHORITY = "resourceAuthorityStrings";

    private static final String DEFAULT_ROLE_AUTHORITY = "roleAuthorityStrings";

    /**
     * 默认 socket 链接头的设备唯一识别名称
     */
    public static final String DEFAULT_IDENTIFIED_HEADER_NAME = "io";
    /**
     * 当 socket 链接时的通知事件
     */
    public static final String CONNECTED_EVENT_NAME = "SOCKET_CONNECTED_EVENT";
    /**
     * 当 socket 断开链接时的通知事件
     */
    public static final String DISCONNECTED_EVENT_NAME = "SOCKET_DISCONNECTED_EVENT";
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
    private SocketClientTemplate socketClientTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private List<MessageSender> messageSenderList;

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Autowired
    private SocketServerProperties socketServerProperties;

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private AuthenticationProperties authenticationProperties;

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
                socketServerProperties.getNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                socketServerProperties.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "注册链接 socket 服务 {} 到 nacos，端口为: {}",
                socketServerProperties.getNacosInstanceName(),
                socketServer.getConfiguration().getPort()
        );
    }

    @Override
    public void destroy() throws NacosException {

        NamingService naming = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());

        naming.deregisterInstance(
                socketServerProperties.getNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                socketServerProperties.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "从 nacos 关闭 链接 socket 服务 {}，端口为: {}",
                socketServerProperties.getNacosInstanceName(),
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

        String username = data.getSingleUrlParam(authenticationProperties.getUsernameParamName());
        String password = data.getSingleUrlParam(authenticationProperties.getPasswordParamName());

        String deviceIdentified = data.getHttpHeaders().get(DEFAULT_IDENTIFIED_HEADER_NAME);
        String userId = data.getHttpHeaders().get(authenticationProperties.getDeviceId().getAccessUserIdHeaderName());

        try {

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add(authenticationProperties.getUsernameParamName(), username);
            body.add(authenticationProperties.getPasswordParamName(), password);

            ResponseEntity<Map<String, Object>> result = Casts.cast(
                    restTemplate.postForEntity(
                            socketServerProperties.getValidTokenUrl(),
                            DeviceIdContextRepository.ofHttpEntity(body, deviceIdentified, userId),
                            Map.class
                    )
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

            Map<String, Object> userResult = Casts.cast(resultData);
            if (MapUtils.isEmpty(userResult)) {
                return false;
            }

            SocketUserDetails user = Casts.convertValue(userResult, SocketUserDetails.class);
            if(!UserStatus.Enabled.getValue().equals(userResult.get(RestResult.DEFAULT_STATUS_NAME))) {
                log.warn("ID 为 [" + userId + "] 的用户非启用");
                return false;
            }

            List<String> resourceAuthorityList = Casts.cast(userResult.get(DEFAULT_RESOURCE_AUTHORITY));
            user.setResourceAuthorities(
                    resourceAuthorityList
                            .stream()
                            .map(ResourceAuthority::new)
                            .collect(Collectors.toList())
            );

            List<String> roleAuthorityList = Casts.cast(userResult.get(DEFAULT_ROLE_AUTHORITY));
            user.setRoleAuthorities(
                    roleAuthorityList
                            .stream()
                            .map(RoleAuthority::new)
                            .collect(Collectors.toList())
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

            // 设置登陆账户和密码以及状态
            user.setUsername(username);
            user.setPassword(password);
            user.setConnectStatus(ConnectStatus.Connecting.getValue());

            // 设置 socket server 的 id 地址和端口
            user.setSocketServerIp(discoveryProperties.getIp());
            user.setPort(socketServerProperties.getPort());

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
                log.debug("ID 为 [{}] 的用户在其他设备登录，断开上一次设备的链接，响应结果为:{}", details.getId(), result);
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

        if (Objects.isNull(user)) {
            client.disconnect();
            return ;
        }

        // 设置链接状态为已连接
        user.setConnectStatus(ConnectStatus.Connect.getValue());
        // 设置最后链接时间
        user.setConnectTime(new Date());

        refreshSpringSecurityContext(user);

        BroadcastMessage<String> message = BroadcastMessage.of(
                CONNECTED_EVENT_NAME,
                Casts.writeValueAsString(RestResult.ofSuccess(user))
        );

        socketClientTemplate.broadcast(message);

        log.info("设备: " + deviceIdentified + "建立连接成功, " + "IP 为: "
                + client.getRemoteAddress().toString() + ", 用户为: " + user.getId());

    }

    @Override
    public void onDisconnect(SocketIOClient client) {

        String uuid = client.getSessionId().toString();

        String deviceIdentified = client.getHandshakeData().getHttpHeaders().get(DEFAULT_IDENTIFIED_HEADER_NAME);
        RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);

        SecurityContext securityContext = bucket.get();

        if (Objects.isNull(securityContext)) {
            return ;
        }

        SocketUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

        // 删除 spring security 用户信息
        bucket.deleteAsync();

        BroadcastMessage<String> message = BroadcastMessage.of(
                DISCONNECTED_EVENT_NAME,
                Casts.writeValueAsString(RestResult.ofSuccess(user))
        );

        socketClientTemplate.broadcast(message);

        log.info("IP: {} UUID: {} 设备断开连接 ,用户为: {}" , client.getRemoteAddress().toString(), uuid, user.getId());

    }
}
