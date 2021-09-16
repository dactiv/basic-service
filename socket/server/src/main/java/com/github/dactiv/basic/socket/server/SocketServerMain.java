package com.github.dactiv.basic.socket.server;

import com.github.dactiv.basic.socket.server.config.SocketServerConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 服务启动类
 *
 * @author maurice
 */
@EnableWebSecurity
@EnableDiscoveryClient
@EnableRedisHttpSession
@Import(SocketServerConfig.class)
@EnableFeignClients("com.github.dactiv.basic.commons.feign")
@SpringBootApplication(scanBasePackages = "com.github.dactiv.basic.socket.server", exclude = DataSourceAutoConfiguration.class)
public class SocketServerMain {
}
