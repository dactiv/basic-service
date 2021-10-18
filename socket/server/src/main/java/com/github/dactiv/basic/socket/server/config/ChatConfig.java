package com.github.dactiv.basic.socket.server.config;


import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import io.minio.GetObjectArgs;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 聊天配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("dactiv.socket.chat")
public class ChatConfig {

    /**
     * 加解密类型
     */
    private String cryptoType = "AES";
    /**
     * 加解密密钥
     */
    private String cryptoKey = "+ZxmXBvLTtCNv0r56Sgxfg==";
    /**
     * 全局文件消息配置
     */
    private Global global = new Global();
    /**
     * 单体消息配置
     */
    private Message message = new Message();

    /**
     * 全局文件配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Global {

        /**
         * 桶名称
         */
        private String bucketName = "chat.global";

        /**
         * 文件 token
         */
        private String fileToken = "global_message_{0}_and_{1}.json";

        /**
         * 缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "socket:server:chat:global:",
                new TimeProperties(1, TimeUnit.DAYS)
        );
    }

    /**
     * 联系人配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Contact {

    }

    /**
     * 消息配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Message {

        /**
         * 桶名称
         */
        private String bucketName = "chat.contact";

        /**
         * 文件 token
         */
        private String fileToken = "info_{0}.json";

        /**
         * 文件后缀
         */
        private String fileSuffix = "yyyyMMddHHmmssms";

        /**
         * 常用联系人文件 token
         */
        private String contactFileToken = "contact_{0}_and_{1}.json";

        /**
         * 近期联系人文件 token
         */
        private String recentContactFileToken = "recent_contact_{0}.json";

        /**
         * 缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "socket:server:chat:contact:",
                new TimeProperties(1, TimeUnit.DAYS)
        );

        /**
         * 分页大小
         */
        private Integer pageSize = 30;

        /**
         * 每个文件存储的信息大小
         */
        private Integer batchSize = 1000;
    }
}
