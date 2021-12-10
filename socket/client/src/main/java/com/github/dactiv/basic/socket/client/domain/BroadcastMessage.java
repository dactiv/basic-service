package com.github.dactiv.basic.socket.client.domain;

import com.github.dactiv.framework.commons.RestResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 广播 socket 消息对象
 *
 * @param <T> 发送的数据类型
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BroadcastMessage<T> extends SocketMessage<T> {

    private static final long serialVersionUID = 2192204554091742380L;

    public static final String DEFAULT_TYPE = "broadcast";

    /**
     * 房间频道，如果为空全网广播
     */
    private String room;

    public static <T> BroadcastMessage<T> of(String room, String event, T data) {

        return of(room, event, RestResult.ofSuccess(data));
    }

    public static <T> BroadcastMessage<T> of(String event, T data) {
        return of(event, RestResult.ofSuccess(data));
    }

    public static <T> BroadcastMessage<T> of(String room, String event, RestResult<T> message) {

        BroadcastMessage<T> result = new BroadcastMessage<>(room);

        result.setMessage(message);
        result.setEvent(event);

        return result;
    }

    public static <T> BroadcastMessage<T> of(String event, RestResult<T> message) {

        return of(null, event, message);
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }
}
