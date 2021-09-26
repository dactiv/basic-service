package com.github.dactiv.basic.socket.client.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 单数据循环单播 socket 消息实现, 该对象和 {@link MultipleUnicastMessage} 区别在于:本对象能直接知道 socket 服务器的具体 ip，可以直接发送。
 * 而 {@link MultipleUnicastMessage} 在 {@link com.github.dactiv.basic.socket.client.SocketClientTemplate} 里根据所有的服务发现去循环发送，
 * 不管该设备识别在不在当前服务器。
 *
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "ofSocketUserDetails")
public class MultipleSocketUserMessage<T> extends MultipleUnicastMessage<T>{

    private static final long serialVersionUID = 6233942402525531392L;

    /**
     * 客户端事件集合
     */
    @NonNull
    @JsonIgnore
    private List<SocketUserDetails> socketUserDetails = new LinkedList<>();

    public static <T> MultipleSocketUserMessage<T> ofSocketUserDetails(List<SocketUserDetails> socketUserDetails, String event, T data) {

        return ofSocketUserDetails(socketUserDetails, event, RestResult.ofSuccess(data));
    }

    public static <T> MultipleSocketUserMessage<T> ofSocketUserDetails(List<SocketUserDetails> socketUserDetails, String event, RestResult<T> message) {

        List<String> deviceIdentifiedList = socketUserDetails
                .stream()
                .map(MobileUserDetails::getDeviceIdentified)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        MultipleUnicastMessage<T> source = MultipleUnicastMessage.of(deviceIdentifiedList, event, message);
        MultipleSocketUserMessage<T> target = new MultipleSocketUserMessage<>(socketUserDetails);

        BeanUtils.copyProperties(source, target);

        target.setSocketUserDetails(socketUserDetails);

        return target;
    }
}
