package com.fuyu.basic.message.service.support;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.enumerate.support.ExecuteStatus;
import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.retry.Retryable;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.message.RabbitmqConfig;
import com.fuyu.basic.message.dao.entity.SmsMessage;
import com.fuyu.basic.message.service.AbstractMessageSender;
import com.fuyu.basic.message.service.MessageService;
import com.fuyu.basic.message.service.support.sms.SmsChannelSender;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import reactor.util.retry.Retry;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 短信消息发送者实现
 *
 * @author maurice
 */
@Component
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

        data.forEach(entity -> {

            entity.setLastSendTime(new Date());

            SmsChannelSender smsChannelSender = getSmsChannelSender(this.channel);

            entity.setChannel(smsChannelSender.getType());

            try {

                RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

                if (restResult.getStatus() == HttpStatus.OK.value()) {
                    entity.setStatus(ExecuteStatus.Success.getValue());
                    entity.setSuccessTime(new Date());
                    entity.setRemark(String.format("%s:%s", restResult.getMessage(), restResult.getData()));
                } else {
                    entity.setStatus(ExecuteStatus.Failure.getValue());
                    entity.setException(restResult.getMessage());
                }
            } catch (Exception e) {
                entity.setStatus(ExecuteStatus.Failure.getValue());
                entity.setException(e.getMessage());
            }

            if (ExecuteStatus.Failure.getValue().equals(entity.getStatus()) && entity.isRetry()) {

                entity.setRetryCount(entity.getRetryCount() + 1);
                messageService.saveSmsMessage(entity);

                amqpTemplate.convertAndSend(
                        RabbitmqConfig.DEFAULT_DELAY_EXCHANGE,
                        DEFAULT_QUEUE_NAME,
                        Collections.singletonList(entity),
                        message -> {
                            message.getMessageProperties().setDelay(entity.getNextIntervalTime());
                            return message;
                        }
                );

            } else {

                messageService.saveSmsMessage(entity);

            }

        });
    }

    private SmsChannelSender getSmsChannelSender(String channel) {
        Optional<SmsChannelSender> smsChannelSender = smsChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst();

        if (!smsChannelSender.isPresent()) {
            throw new ServiceException("找不到渠道为[" + channel + "]的短信渠道支持");
        }

        return smsChannelSender.get();
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<SmsMessage> entity) {

        try {

            messageService.saveSmsMessages(entity);

            amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entity);

            return new RestResult<>(
                    "发送短信成功",
                    HttpStatus.OK.value(),
                    RestResult.SUCCESS_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );

        } catch (Exception e) {
            return new RestResult<>(RestResult.ERROR_EXECUTE_CODE, e);
        }


    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
