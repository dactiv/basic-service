package com.github.dactiv.basic.socket.server.config;

import com.corundumstudio.socketio.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * socket io 服务配置
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("dactiv.socket.server")
public class SocketServerProperties extends Configuration {

    public static final String DEFAULT_SERVER_ID_PARAM_NAME = "serverId";

    public static final String DEFAULT_NACOS_INSTANCE_NAME = "netty-socket-server";

    /**
     * 校验用户信息地址
     */
    private String validTokenUrl;

    /**
     * nacos 实例名称
     */
    private String nacosInstanceName = DEFAULT_NACOS_INSTANCE_NAME;
}
