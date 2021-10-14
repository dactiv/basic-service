package com.github.dactiv.basic.socket.server.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.commons.utils.MinioUtils;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.SocketUserDetailsContextRepository;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import io.minio.MinioClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * socket 服务配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(ApplicationConfig.class)
public class ApplicationStartupAutoConfig implements InitializingBean {

    @Autowired
    private MinioClient minioClient;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * minio 客户端
     *
     * @param applicationConfig mini 模版
     *
     * @return mini 模版
     */
    @Bean
    public MinioClient minioClient(ApplicationConfig applicationConfig) {
        return MinioClient
                .builder()
                .endpoint(applicationConfig.getMinio().getEndpoint())
                .credentials(applicationConfig.getMinio().getAccessKey(), applicationConfig.getMinio().getSecretKey())
                .build();
    }

    /**
     * socket io 服务配置
     *
     * @param applicationConfig          配置信息
     * @param socketServerManager socket 服务管理
     *
     * @return socket io 服务
     */
    @Bean
    public SocketIOServer socketIoServer(ApplicationConfig applicationConfig, SocketServerManager socketServerManager) {

        applicationConfig.setAuthorizationListener(socketServerManager);

        SocketIOServer socketIoServer = new SocketIOServer(applicationConfig);

        socketIoServer.addConnectListener(socketServerManager);
        socketIoServer.addDisconnectListener(socketServerManager);

        return socketIoServer;
    }

    /**
     * 用 SocketUserDetailsContextRepository 替代 DeviceIdContextRepository
     *
     * @param properties 配置信息
     * @param redissonClient redisson 客户端
     *
     * @return SocketUserDetailsContextRepository
     */
    @Bean
    public SocketUserDetailsContextRepository socketUserDetailsContextRepository(AuthenticationProperties properties,
                                                                                 RedissonClient redissonClient) {
        return new SocketUserDetailsContextRepository(properties, redissonClient);
    }

    @Override
    public void afterPropertiesSet() {
        MinioUtils.setMinioClient(minioClient);
        MinioUtils.setObjectMapper(objectMapper == null ? new ObjectMapper() : objectMapper);
    }
}
