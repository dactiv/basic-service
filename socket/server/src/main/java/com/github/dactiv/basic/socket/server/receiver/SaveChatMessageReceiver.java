package com.github.dactiv.basic.socket.server.receiver;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.config.ApplicationConfig;
import com.github.dactiv.basic.socket.server.service.chat.ContactMessage;
import com.rabbitmq.client.Channel;
import io.minio.MinioClient;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 保存聊天信息 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class SaveChatMessageReceiver implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "save.chat.message";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ApplicationConfig applicationConfig;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload ContactMessage message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        channel.basicAck(tag, false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        applicationConfig.getChat().getGlobalMessageBucketName();
    }
}
