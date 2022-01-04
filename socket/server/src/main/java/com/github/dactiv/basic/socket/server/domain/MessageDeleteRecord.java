package com.github.dactiv.basic.socket.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 群聊消息删除记录 dto, 用于记录用户在群聊中已删除的记录信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class MessageDeleteRecord {

    /**
     * 用户 id
     */
    @NonNull
    private Integer userId;

    /**
     * 目标 id
     */
    @NonNull
    private Integer targetId;

    /**
     * 已删除的记录
     */
    private Map<String, List<String>> deleteMap = new LinkedHashMap<>();

}
