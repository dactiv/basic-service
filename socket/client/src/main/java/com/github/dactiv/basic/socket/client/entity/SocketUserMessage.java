package com.github.dactiv.basic.socket.client.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.RestResult;
import lombok.*;
import org.springframework.beans.BeanUtils;

/**
 * 单播 socket 用户消息对象, 该对象和 {@link UnicastMessage} 区别在于:本对象能直接知道 socket 服务器的具体 ip，可以直接发送。
 * 而 {@link UnicastMessage} 在 {@link com.github.dactiv.basic.socket.client.SocketClientTemplate} 里根据所有的服务发现去循环发送，
 * 不管该设备识别在不在当前服务器。
 *
 * @param <T> 消息数据类型
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SocketUserMessage<T> extends UnicastMessage<T> {

    private static final long serialVersionUID = 5389291016612742370L;

    /**
     * 设备唯一识别
     */
    @NonNull
    @JsonIgnore
    private SocketUserDetails details;

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    public static <T> SocketUserMessage<T> of(SocketUserDetails details, String event, RestResult<T> message) {

        UnicastMessage<T> source = UnicastMessage.of(details.getDeviceIdentified(), event, message);

        SocketUserMessage<T> target = new SocketUserMessage<>();

        BeanUtils.copyProperties(source, target);

        target.setDetails(details);

        return target;
    }

    public static <T> SocketUserMessage<T> of(SocketUserDetails details, String event, T data) {

        return of(details, event, RestResult.ofSuccess(data));
    }

}
