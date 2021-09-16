package com.github.dactiv.basic.socket.server.service;

import com.github.dactiv.basic.socket.server.config.SocketServerConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 远程 api 服务，用于在调用公共服务时使用的类库
 *
 * @author maurice
 */
@Component
public class RemoteApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Getter
    @Autowired
    private SocketServerConfig socketServerProperties;

    /**
     * 调用远程服务验证码用户
     *
     * @param username 用户名
     * @param password 密码
     * @param userId 用户 id
     * @param deviceIdentified 设备唯一识别
     *
     * @return 用户信息
     */
    public ResponseEntity<Map<String, Object>> validMobileUserDetails(String username, String password, String userId, String deviceIdentified) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY, username);
        body.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY, password);

        return Casts.cast(
                restTemplate.postForEntity(
                        socketServerProperties.getValidTokenUrl(),
                        DeviceIdContextRepository.ofHttpEntity(body, deviceIdentified, userId),
                        Map.class
                )
        );
    }

    /**
     * 当用户链接成功后调用的接口信息（用于告知后台，让用户下次链接前直接选择当前服务器信息）
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return 执行结果
     */
    public ResponseEntity<Map<String, Object>> onUserConnect(String deviceIdentified, Integer userId) {

        return Casts.cast(
                restTemplate.postForEntity(
                        socketServerProperties.getOnUserConnectUrl(),
                        createBasicAuthHttpEntity(deviceIdentified, userId),
                        Map.class
                )
        );
    }

    /**
     * 当用户链接断开后调用的接口信息（用于告知后台，让用户下次链接前直接选择当前服务器信息）
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return 执行结果
     */
    public ResponseEntity<Map<String, Object>> onUserDisconnect(String deviceIdentified, Integer userId) {

        return Casts.cast(
                restTemplate.postForEntity(
                        socketServerProperties.getOnUserDisconnectUrl(),
                        createBasicAuthHttpEntity(deviceIdentified, userId),
                        Map.class
                )
        );
    }

    /**
     * 创建带 basic 认证头信息的 http 实体
     *
     * @param deviceIdentified 设备唯一识别
     * @param userId 用户 id
     *
     * @return http 实体
     */
    private HttpEntity<MultiValueMap<String, Object>> createBasicAuthHttpEntity(String deviceIdentified, Integer userId) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(SocketServerConfig.DEFAULT_SERVER_ID_PARAM_NAME, socketServerProperties.getId());

        return DeviceIdContextRepository.ofHttpEntity(body, deviceIdentified, userId);
    }

}
