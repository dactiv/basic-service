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

    public static final String DEFAULT_SERVER_ID_PARAM_NAME = "serverId";

    public static final String DEFAULT_NACOS_INSTANCE_NAME = "netty-socket-server";

    public static final String DEFAULT_ROOM_PREFIX = "socket:room:";

    /**
     * 校验用户信息地址
     */
    private String validTokenUrl;

    /**
     * nacos 实例名称
     */
    private String nacosInstanceName = DEFAULT_NACOS_INSTANCE_NAME;

    /**
     * 房间名前缀
     */
    private String roomPrefix = DEFAULT_ROOM_PREFIX;

    /**
     * 聊天配置
     */
    private Chat chat = new Chat();

    /**
     * 聊天配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    public static class Chat {
        /**
         * 最后展示的消息总条数
         */
        private Integer lastMessageCount = 100;
        /**
         * 全局消息文件 token
         */
        private String globalMessageFileToken = "global_message_{min}_and_{max}_{suffix}.json";
        /**
         * 联系人消息文件 token
         */
        private String contactMessageFileToken = "contact_message_{id}.json";
        /**
         * 常用联系人文件 token
         */
        private String contactLinkFileToken = "contact_like_{id}.json";
        /**
         * 全局消息的桶名称
         */
        private String globalMessageBucketName = "chat.global";
        /**
         * 联系人消息的桶名称
         */
        private String contactMessageBucketName = "chat.contact";
        /**
         * 加解密类型
         */
        private String cryptoType = "AES";
        /**
         * 加解密密钥
         */
        private String cryptoKey = "+ZxmXBvLTtCNv0r56Sgxfg==";
    }
}
