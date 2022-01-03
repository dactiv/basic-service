package com.github.dactiv.basic.socket.server.config;


import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.minio.data.Bucket;
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
     * 联系人配置
     */
    private Contact contact = new Contact();

    /**
     * 群聊配置
     */
    private Group group = new Group();

    /**
     * 消息配置
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
        private Bucket bucket = Bucket.of("socket.server.chat.global");

        /**
         * 保存历史聊天消息的文件数量
         */
        private Integer historyMessageFileCount = 100;

        /**
         * 用户对用户的文件 token
         */
        private String personFileToken = "global_message_{0}_and_{1}.json";

        /**
         * 群聊的文件 token
         */
        private String groupFileToken = "global_group_message_{0}.json";

        /**
         * 缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "socket:server:chat:global:",
                new TimeProperties(7, TimeUnit.DAYS)
        );
    }

    /**
     * 群聊配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Group {
        /**
         * 保存历史聊天消息的文件数量
         */
        private Integer historyMessageFileCount = 3000;

        /**
         * 联系人桶信息
         */
        private Bucket bucket = Bucket.of("socket.server.chat.group");

        /**
         * 常用联系人文件 token
         */
        private String fileToken = "group_{0}.json";

        /**
         * 全局消息缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "socket:server:chat:group:",
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
        /**
         * 联系人桶信息
         */
        private Bucket contactBucket = Bucket.of("socket.server.chat.contact");
        /**
         * 常用联系人桶信息
         */
        private Bucket recentBucket = Bucket.of("socket.server.chat.contact.recent");
        /**
         * 未读消息桶信息
         */
        private Bucket unreadBucket = Bucket.of("socket.server.chat.contact.unread");

        /**
         * 保存历史聊天消息的文件数量
         */
        private Integer historyMessageFileCount = 3000;

        /**
         * 常用联系人文件 token
         */
        private String fileToken = "contact_{0}_and_{1}.json";

        /**
         * 近期联系人文件 token
         */
        private String recentFileToken = "recent_contact_{0}.json";

        /**
         * 未读消息文件 token
         */
        private String unreadMessageFileToken = "unread_message_{0}.json";

        /**
         * 常用联系人存储个数
         */
        private Integer recentCount = 20;

        /**
         * 全局消息缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "socket:server:chat:contact:",
                new TimeProperties(1, TimeUnit.DAYS)
        );

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
         * 消息桶信息
         */
        private Bucket bucket = Bucket.of("socket.server.chat.message");

        /**
         * 文件 token
         */
        private String fileToken = "info_{0}.json";

        /**
         * 文件后缀
         */
        private String fileSuffix = "yyyyMMddHHmmssSSS";

        /**
         * 分页大小
         */
        private Integer pageSize = 30;

        /**
         * 每个文件存储的信息大小
         */
        private Integer batchSize = 10000;
    }
}
