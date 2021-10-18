package com.github.dactiv.basic.socket.server.receiver.chat;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

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

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload ReadMessageRequestBody body,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        channel.basicAck(tag, false);
    }
}
