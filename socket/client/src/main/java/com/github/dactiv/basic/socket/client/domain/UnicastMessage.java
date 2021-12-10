package com.github.dactiv.basic.socket.client.domain;

import com.github.dactiv.framework.commons.RestResult;
import lombok.*;

/**
 * 单播 socket 消息对象
 *
 * @param <T>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnicastMessage<T> extends SocketMessage<T> {

    private static final long serialVersionUID = 6202588511755627871L;

    public static final String DEFAULT_TYPE = "unicast";

    /**
     * 设备唯一识别
     */
    @NonNull
    private String deviceIdentified;

    public static <T> UnicastMessage<T> of(String deviceIdentified, String event, T data) {

        return of(deviceIdentified, event, RestResult.ofSuccess(data));
    }

    public static <T> UnicastMessage<T> of(String deviceIdentified, String event, RestResult<T> message) {

        UnicastMessage<T> result = new UnicastMessage<>(deviceIdentified);

        result.setMessage(message);
        result.setEvent(event);

        return result;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }
}
