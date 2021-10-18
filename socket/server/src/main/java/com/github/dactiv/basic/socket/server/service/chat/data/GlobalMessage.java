package com.github.dactiv.basic.socket.server.service.chat.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 联系人实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GlobalMessage extends BasicMessage  {

    private static final long serialVersionUID = 6466725003068761373L;

    public static final String DEFAULT_MESSAGE_IDS = "messageIds";

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 当前文件名
     */
    private String currentFile;

    /**
     * 消息文件, key 为文件名, value 为消息总数
     */
    private Map<String, Integer> messageFileMap = new LinkedHashMap<>();
}
