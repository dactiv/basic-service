package com.github.dactiv.basic.socket.server.domain.meta;

import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局消息元数据实现
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GlobalMessageMeta extends BasicMessageMeta {

    private static final long serialVersionUID = 6466725003068761373L;

    public static final String DEFAULT_MESSAGE_IDS = "messageIds";

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 类型
     */
    private MessageTypeEnum type;

    /**
     * 当前文件名
     */
    private String currentMessageFile;

    /**
     * 消息文件, key 为文件名, value 为消息总数
     */
    private Map<String, Integer> messageFileMap = new LinkedHashMap<>();
}
