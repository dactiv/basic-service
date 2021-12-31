package com.github.dactiv.basic.socket.server.receiver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.basic.socket.server.service.chat.resolver.MessageResolver;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.minio.data.FileObject;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 读取消息 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class ReadMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "read.chat.message";

    private final List<MessageResolver> messageResolvers;

    public ReadMessageReceiver(ObjectProvider<MessageResolver> messageResolvers) {
        this.messageResolvers = messageResolvers.orderedStream().collect(Collectors.toList());
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload ReadMessageRequestBody body,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        readMessage(body);

        channel.basicAck(tag, false);
    }

    private void readMessage(ReadMessageRequestBody body) throws Exception {

        MessageResolver messageResolver = messageResolvers
                .stream()
                .filter(r -> r.isSupport(body.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + body.getType().getValue() + "] 的消息解析器"));

        messageResolver.consumeReadMessage(
                body.getSenderId(),
                body.getRecipientId(),
                body.getMessageIds(),
                body.getCreationTime()
        );


    }

}
