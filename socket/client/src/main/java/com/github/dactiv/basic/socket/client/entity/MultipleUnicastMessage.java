package com.github.dactiv.basic.socket.client.entity;

import com.github.dactiv.framework.commons.RestResult;
import lombok.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单数据循环单播 socket 消息实现， 用于在存在一个数据内容，发送给多个用户的时候使用
 *
 * @param <T>
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class MultipleUnicastMessage<T> extends SocketMessage<T> {

    private static final long serialVersionUID = 6233942402525531392L;

    public static final String DEFAULT_TYPE = "multipleUnicast";

    /**
     * 客户端事件集合
     */
    @NonNull
    private List<String> deviceIdentifiedList = new LinkedList<>();

    /**
     * 转换为单播 socket 消息
     *
     * @return 单播 socket 消息集合
     */
    public List<UnicastMessage<?>> toUnicastMessageList() {
        return deviceIdentifiedList
                .stream()
                .map(c -> UnicastMessage.of(c, getEvent(), getMessage()))
                .collect(Collectors.toList());
    }

    public static <T> MultipleUnicastMessage<T> of(List<String> deviceIdentifiedList, String event, T data) {

        return of(deviceIdentifiedList, event, RestResult.ofSuccess(data));
    }

    public static <T> MultipleUnicastMessage<T> of(List<String> deviceIdentifiedList, String event, RestResult<T> message) {
        MultipleUnicastMessage<T> result = of(deviceIdentifiedList);
        result.setEvent(event);
        result.setMessage(message);
        return result;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }
}
