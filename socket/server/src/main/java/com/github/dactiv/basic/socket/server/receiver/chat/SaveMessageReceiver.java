package com.github.dactiv.basic.socket.server.receiver.chat;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.enumerate.ContactType;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 保存聊天信息 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class SaveMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "save.chat.message";

    @Autowired
    private ChatService chatService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload ContactMessage<?> contactMessage,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        // 添加双方常用联系人信息
        chatService.addRecentContact(contactMessage.getId(), contactMessage.getTargetId(), ContactType.Person);
        chatService.addRecentContact(contactMessage.getTargetId(), contactMessage.getId(), ContactType.Person);

        channel.basicAck(tag, false);

    }
}
