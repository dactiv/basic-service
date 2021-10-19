package com.github.dactiv.basic.socket.server.config;

import com.corundumstudio.socketio.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("dactiv.socket.server")
public class ApplicationConfig extends Configuration {

    /**
     * 校验用户信息地址
     */
    private String validTokenUrl;

    /**
     * nacos 实例名称
     */
    private String nacosInstanceName = "netty-socket-server";

    /**
     * 房间名前缀
     */
    private String roomPrefix = "socket:room:";
}
