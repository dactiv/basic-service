package com.github.dactiv.basic.message.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 附件配置
 *
 * @author maurice.chen
 */
@Data
@Component
@EqualsAndHashCode
@NoArgsConstructor
@ConfigurationProperties("dactiv.message.attachment")
public class AttachmentConfig {

    /**
     * 桶前缀
     */
    private String bucketPrefix = "message.attachment.";

    /**
     * 响应结果集配置
     */
    private Result result = new Result();

    /**
     * 获取桶名称
     *
     * @param type 桶类型
     *
     * @return 桶名称
     */
    public String getBucketName(String type) {
        return bucketPrefix + type;
    }

    /**
     * 响应结果集配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Result {

        /**
         * 上传文件要忽略的响应字段
         */
        private List<String> uploadResultIgnoreFields = Collections.singletonList("headers");

        /**
         * 链接 uri
         */
        private String linkUri = "http://localhost:8080/message/attachment/get/{0}/{1}";

        /**
         * 链接参数名称
         */
        private String linkParamName = "link";
    }
}
