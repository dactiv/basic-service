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
public class SocketServerConfig extends Configuration {

    public static final String DEFAULT_SERVER_ID_PARAM_NAME = "serverId";

    /**
     * 校验用户信息地址
     */
    private String validTokenUrl;

    /**
     * 链接 socket 成功后的通知地址
     */
    private String onUserConnectUrl;

    /**
     * 断开 socket 链接后的通知地址
     */
    private String onUserDisconnectUrl;

    /**
     * 服务器 id
     */
    private Integer id;
    /**
     * socket 服务名称
     */
    private String serverName = "socket";
}
