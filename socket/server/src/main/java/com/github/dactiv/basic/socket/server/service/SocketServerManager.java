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
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.domain.SocketMessage;
import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.basic.socket.client.domain.SocketUserMessage;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.server.config.ApplicationConfig;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomEntity;
import com.github.dactiv.basic.socket.server.service.message.MessageSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.FileObject;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.ResourceAuthority;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
import java.text.MessageFormat;
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
     * 默认客户端提交的备唯一识别名称
     */
    public static final String DEFAULT_CLIENT_IDENTIFIED_PARAM_NAME = "did";
    /**
     * 默认客户端提交的用户 id 参数
     */
    public static final String DEFAULT_USER_ID_PARAM_NAME = "uid";
    /**
     * 当 socket 服务主动断开时的通知事件
     */
    public static final String SERVER_DISCONNECT_EVENT_NAME = "socket_server_disconnect";
    /**
     * 当 socket 服务要求客户端断开时的通知事件
     */
    public static final String CLIENT_DISCONNECT_EVENT_NAME = "client_disconnect";

    private DeviceIdContextRepository securityContextRepository;

    private SocketClientTemplate socketClientTemplate;

    private RestTemplate restTemplate;

    private SocketIOServer socketServer;

    private List<MessageSender> messageSenderList;

    private NacosServiceManager nacosServiceManager;

    private ApplicationConfig applicationConfig;

    private NacosDiscoveryProperties discoveryProperties;

    private AuthenticationProperties authenticationProperties;

    private RoomService roomService;

    private RedissonClient redissonClient;

    private MinioTemplate minioTemplate;

    /**
     * 发送消息
     *
     * @param message socket 消息
     */
    public void sendMessage(SocketMessage<?> message) {
        messageSenderList.stream().filter(m -> m.isSupport(message)).forEach(m -> m.sendMessage(message, socketServer));
    }

    /**
     * 加入房间
     *
     * @param userDetails socket 用户明细
     * @param room 房间名称
     */
    public void joinRoom(SocketUserDetails userDetails, String room) {
        joinRoom(userDetails, Collections.singletonList(room));
    }

    /**
     * 加入聊天房间频道
     *
     * @param userDetails socket 用户明细
     * @param rooms 房间名称集合
     */
    public void joinRoom(SocketUserDetails userDetails, List<String> rooms) {

        SocketIOClient client = socketServer.getClient(UUID.fromString(userDetails.getDeviceIdentified()));

        if (Objects.isNull(client) ) {
            return ;
        }

        Set<String> allRooms = client.getAllRooms();
        rooms.stream().filter(r -> !allRooms.contains(r)).forEach(client::joinRoom);
    }

    /**
     * 离开聊天房间频道
     *
     * @param userDetails socket 用户明细
     * @param rooms 房间名称集合
     */
    public void leaveRoom(SocketUserDetails userDetails, List<String> rooms) {
        SocketIOClient client = socketServer.getClient(UUID.fromString(userDetails.getDeviceIdentified()));

        if (Objects.isNull(client) ) {
            return ;
        }

        Set<String> allRooms = client.getAllRooms();

        rooms.stream().filter(allRooms::contains).forEach(client::leaveRoom);
    }

    @Override
    public void run(String... args) throws NacosException {

        socketServer.start();

        log.info("已启动 socket 服务，端口为:" + socketServer.getConfiguration().getPort());

        NamingService naming = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());

        naming.registerInstance(
                applicationConfig.getNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                applicationConfig.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "注册链接 socket 服务 {} 到 nacos，端口为: {}",
                applicationConfig.getNacosInstanceName(),
                socketServer.getConfiguration().getPort()
        );
    }

    @Override
    public void destroy() throws NacosException {

        NamingService naming = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());

        naming.deregisterInstance(
                applicationConfig.getNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                applicationConfig.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "从 nacos 关闭 链接 socket 服务 {}，端口为: {}",
                applicationConfig.getNacosInstanceName(),
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

        String deviceIdentified = data.getSingleUrlParam(DEFAULT_CLIENT_IDENTIFIED_PARAM_NAME);
        String userId = data.getSingleUrlParam(DEFAULT_USER_ID_PARAM_NAME);

        data.getHttpHeaders().add(DEFAULT_IDENTIFIED_HEADER_NAME, deviceIdentified);

        try {

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add(authenticationProperties.getUsernameParamName(), username);
            body.add(authenticationProperties.getPasswordParamName(), password);

            ResponseEntity<Map<String, Object>> result = Casts.cast(
                    restTemplate.postForEntity(
                            applicationConfig.getValidTokenUrl(),
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
            if (userResult.containsKey(NumberIdEntity.CREATION_TIME_FIELD_NAME)) {
                Long createTimeValue = Casts.cast(userResult.get(NumberIdEntity.CREATION_TIME_FIELD_NAME));
                user.setCreationTime(new Date(createTimeValue));
            }
            if (!UserStatus.Enabled.equals(user.getStatus())) {
                log.warn("ID 为 [" + userId + "] 的用户非启用");
                return false;
            }

            List<String> resourceAuthorityList = Casts.cast(userResult.get(DEFAULT_RESOURCE_AUTHORITY));
            if (CollectionUtils.isNotEmpty(resourceAuthorityList)) {
                user.setResourceAuthorities(
                        resourceAuthorityList
                                .stream()
                                .map(ResourceAuthority::new)
                                .collect(Collectors.toList())
                );
            }

            List<String> roleAuthorityList = Casts.cast(userResult.get(DEFAULT_ROLE_AUTHORITY));
            if (CollectionUtils.isNotEmpty(roleAuthorityList)) {
                user.setRoleAuthorities(
                        roleAuthorityList
                                .stream()
                                .map(RoleAuthority::new)
                                .collect(Collectors.toList())
                );
            }

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
            user.setPort(applicationConfig.getPort());

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

        if (Objects.nonNull(user.getId())) {
            String name = authenticationProperties.getDeviceId().getCache().getName(user.getId().toString());
            RBucket<SocketUserDetails> userDetailsRBucket = redissonClient.getBucket(name);
            userDetailsRBucket.setAsync(user);
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
                    CLIENT_DISCONNECT_EVENT_NAME,
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

        String deviceIdentified = client.getHandshakeData().getSingleUrlParam(DEFAULT_CLIENT_IDENTIFIED_PARAM_NAME);
        SecurityContext securityContext = securityContextRepository.getSecurityContextBucket(deviceIdentified).get();
        if (Objects.isNull(securityContext)) {
            log.warn("在认证通过后连接 socket 时出现获取用户信息为 null 的情况, deviceIdentified 为:" + deviceIdentified);
            client.sendEvent(SERVER_DISCONNECT_EVENT_NAME, Casts.writeValueAsString(RestResult.of("找不到当前认证的用户")));
            client.disconnect();
        }

        SocketUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());
        if (Objects.isNull(user)) {
            client.disconnect();
            return;
        }

        // 设置链接状态为已连接
        user.setConnectStatus(ConnectStatus.Connect.getValue());
        // 设置最后链接时间
        user.setConnectTime(new Date());

        refreshSpringSecurityContext(user);
        // 获取用户房间信息
        List<RoomEntity> rooms = roomService.findByUserId(Casts.cast(user.getId(), Integer.class));
        // 如果存在房间，讲客户端假如房间，做广播使用。
        if (CollectionUtils.isEmpty(rooms)) {
            rooms.forEach(r -> client.joinRoom(r.getId().toString()));
        }

        log.info("设备: " + deviceIdentified + "建立连接成功, " + "IP 为: "
                + client.getRemoteAddress().toString() + ", 用户为: " + user.getId());

    }

    @Override
    public void onDisconnect(SocketIOClient client) {

        String uuid = client.getSessionId().toString();

        String deviceIdentified = client.getHandshakeData().getSingleUrlParam(DEFAULT_CLIENT_IDENTIFIED_PARAM_NAME);
        RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);

        SecurityContext securityContext = bucket.get();

        if (Objects.isNull(securityContext)) {
            return;
        }

        SocketUserDetails user = Casts.cast(securityContext.getAuthentication().getDetails());

        // 删除 spring security 用户信息
        bucket.deleteAsync();

        String name = authenticationProperties.getDeviceId().getCache().getName(user.getId().toString());
        RBucket<SocketUserDetails> userDetailsRBucket = redissonClient.getBucket(name);
        // 删除用户明细信息
        userDetailsRBucket.deleteAsync();

        log.info("IP: {} UUID: {} 设备断开连接 ,用户为: {}", client.getRemoteAddress().toString(), uuid, user.getId());

    }

    /**
     * 清除临时消息
     *
     * @param userId 用户 id
     * @param type 消息类型
     *
     * @throws Exception 清除临时消息
     */
    public void clearTempMessage(Integer userId, String type) throws Exception {
        String filename = MessageFormat.format(applicationConfig.getTempMessageFileToken(), userId, type);
        minioTemplate.deleteObject(FileObject.of(applicationConfig.getTempMessageBucket(), filename));
    }

    /**
     * 获取临时消息
     *
     * @param userId 用户 id
     * @param type 消息类型
     *
     * @return 临时消息集合
     */
    public List<Object> getTempMessages(Integer userId, String type) {
        String filename = MessageFormat.format(applicationConfig.getTempMessageFileToken(), userId, type);

        List<Object> result = minioTemplate.readJsonValue(
                FileObject.of(applicationConfig.getTempMessageBucket(), filename),
                new TypeReference<>() {
                }
        );

        if (CollectionUtils.isEmpty(result)) {
            result = new LinkedList<>();
        }

        return result;
    }

    /**
     * 保存临时消息
     *
     * @param userId 用户 id
     * @param type 消息类型
     * @param tempMessage 临时消息集合
     *
     * @throws Exception 保存错误时抛出
     */
    @Async
    public void saveTempMessage(Integer userId, String type, Object tempMessage) throws Exception {
        if (Objects.isNull(tempMessage)) {
            return ;
        }
        saveTempMessages(userId, type, Collections.singletonList(tempMessage));
    }

    /**
     * 保存临时消息
     *
     * @param userId 用户 id
     * @param type 消息类型
     * @param tempMessages 临时消息集合
     *
     * @throws Exception 保存错误时抛出
     */
    @Async
    public void saveTempMessages(Integer userId, String type, List<Object> tempMessages) throws Exception {
        if (CollectionUtils.isEmpty(tempMessages)) {
            return ;
        }
        String filename = MessageFormat.format(applicationConfig.getTempMessageFileToken(), userId, type);
        List<Object> objects = getTempMessages(userId, type);
        objects.addAll(tempMessages);
        minioTemplate.writeJsonValue(FileObject.of(applicationConfig.getTempMessageBucket(), filename), objects);
    }

    /**
     * 获取 socket 用户明细
     *
     * @param userId 用户 id
     *
     * @return socket 用户明细
     */
    public SocketUserDetails getSocketUserDetails(Integer userId) {
        String name = authenticationProperties.getDeviceId().getCache().getName(userId.toString());
        RBucket<SocketUserDetails> userDetailsRBucket = redissonClient.getBucket(name);

        return userDetailsRBucket.get();
    }

    /**
     * 获取 socket 用户明细
     *
     * @param deviceIdentified 设备唯一识别
     *
     * @return socket 用户明细
     */
    public SocketUserDetails getSocketUserDetails(String deviceIdentified) {
        RBucket<SecurityContext> bucket = securityContextRepository.getSecurityContextBucket(deviceIdentified);
        SecurityContext securityContext = bucket.get();

        if (Objects.isNull(securityContext)) {
            return null;
        }

        return Casts.cast(securityContext.getAuthentication().getDetails());
    }

    @Autowired
    public void setSecurityContextRepository(DeviceIdContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    @Autowired
    public void setSocketClientTemplate(SocketClientTemplate socketClientTemplate) {
        this.socketClientTemplate = socketClientTemplate;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setSocketServer(SocketIOServer socketServer) {
        this.socketServer = socketServer;
    }

    @Autowired
    public void setMessageSenderList(List<MessageSender> messageSenderList) {
        this.messageSenderList = messageSenderList;
    }

    @Autowired
    public void setNacosServiceManager(NacosServiceManager nacosServiceManager) {
        this.nacosServiceManager = nacosServiceManager;
    }

    @Autowired
    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Autowired
    public void setDiscoveryProperties(NacosDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    @Autowired
    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Autowired
    public void setRoomService(RoomService roomService) {
        this.roomService = roomService;
    }

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Autowired
    public void setMinioTemplate(MinioTemplate minioTemplate) {
        this.minioTemplate = minioTemplate;
    }
}
