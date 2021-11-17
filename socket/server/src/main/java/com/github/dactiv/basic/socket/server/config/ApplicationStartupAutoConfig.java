package com.github.dactiv.basic.socket.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.basic.socket.server.service.SocketUserDetailsContextRepository;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.web.query.mybatis.MybatisPlusQueryGenerator;
import org.redisson.api.RedissonClient;
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
public class ApplicationStartupAutoConfig {

    /**
     * socket io 服务配置
     *
     * @param applicationConfig   配置信息
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
     * @param properties     配置信息
     * @param redissonClient redisson 客户端
     *
     * @return SocketUserDetailsContextRepository
     */
    @Bean
    public SocketUserDetailsContextRepository socketUserDetailsContextRepository(AuthenticationProperties properties,
                                                                                 RedissonClient redissonClient) {
        return new SocketUserDetailsContextRepository(properties, redissonClient);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    @Bean
    public CipherAlgorithmService cipherAlgorithmService() {
        return new CipherAlgorithmService();
    }
}
