package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 短信消息发送者实现
 *
 * @author maurice
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SmsMessageSender extends AbstractMessageSender<SmsMessage> {

    public static final String DEFAULT_QUEUE_NAME = "message.sms.queue";

    private static final String DEFAULT_TYPE = "sms";

    @Autowired
    private MessageService messageService;

    @Autowired
    private List<SmsChannelSender> smsChannelSenderList;

    /**
     * 渠道商
     */
    @Value("${spring.sms.channel}")
    private String channel;

    /**
     * 最大重试次数
     */
    @Value("${spring.sms.max-retry-count:3}")
    private Integer maxRetryCount;

    @Override
    protected void afterBindValueSetting(SmsMessage entity, Map<String, Object> value) {
        entity.setMaxRetryCount(maxRetryCount);
    }

    @Override
    protected String getRetryMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
    }

    /**
     * 发送短信
     *
     * @param data 短信实体集合
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, delayed = "true"),
                    key = DEFAULT_QUEUE_NAME
            ),
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void sendSms(@Payload List<SmsMessage> data,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        channel.basicAck(tag, false);

        data.forEach(this::send);
    }

    private void send(SmsMessage entity) {

        entity.setLastSendTime(new Date());

        SmsChannelSender smsChannelSender = getSmsChannelSender(this.channel);

        entity.setChannel(smsChannelSender.getType());

        try {

            RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity,String.format("%s:%s", restResult.getMessage(), restResult.getData()));
            } else {
                ExecuteStatus.failure(entity, restResult.getMessage());
            }

        } catch (Exception e) {
            ExecuteStatus.failure(entity, e.getMessage());
        }

        retry(entity);

        messageService.saveSmsMessage(entity);

        updateBatchMessage(entity);
    }

    private SmsChannelSender getSmsChannelSender(String channel) {
        return smsChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的短信渠道支持"));
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<SmsMessage> entities) {

        entities.forEach(x -> messageService.saveSmsMessage(x));

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entities);

        return RestResult.of("发送短信成功");

    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
