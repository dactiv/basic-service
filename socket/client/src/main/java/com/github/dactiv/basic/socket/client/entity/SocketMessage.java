package com.github.dactiv.basic.socket.client.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.RestResult;
import lombok.*;

import java.io.Serializable;

/**
 * socket 消息对象
 *
 * @author maurice
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class SocketMessage<T> implements Serializable {

    private static final long serialVersionUID = 6021283894540422481L;
    /**
     * 事件类型
     */
    @NonNull
    private String event;

    /**
     * rest 结果集
     */
    @NonNull
    private RestResult<T> message;

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @JsonIgnore
    public abstract String getType();

}
