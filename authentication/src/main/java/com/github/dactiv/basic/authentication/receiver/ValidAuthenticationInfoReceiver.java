package com.github.dactiv.basic.authentication.receiver;

import com.github.dactiv.basic.authentication.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.basic.commons.Constants;
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

import java.io.IOException;

/**
 * 验证认证信息 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class ValidAuthenticationInfoReceiver {

    public static final String DEFAULT_QUEUE_NAME = "authentication.valid.info";

    @Autowired
    private AuthenticationService authenticationService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void validAuthenticationInfo(@Payload AuthenticationInfo info,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        // FIXME 省市县没有通过 ip 分析后进行赋值
        authenticationService.saveAuthenticationInfo(info);
        authenticationService.validAuthenticationInfo(info);

        channel.basicAck(tag, false);

    }
}
