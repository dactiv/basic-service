package com.github.dactiv.basic.socket.server.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * socket 服务配置
 *
 * @author maurice.chen
 */
@Configuration
public class SocketServerConfig {

    /**
     * socket io 服务配置
     *
     * @param properties          配置信息
     * @param socketServerManager socket 服务管理
     *
     * @return socket io 服务
     */
    @Bean
    public SocketIOServer socketIoServer(SocketServerProperties properties, SocketServerManager socketServerManager) {

        properties.setAuthorizationListener(socketServerManager);

        SocketIOServer socketIoServer = new SocketIOServer(properties);

        socketIoServer.addConnectListener(socketServerManager);
        socketIoServer.addDisconnectListener(socketServerManager);


        return socketIoServer;
    }
}
