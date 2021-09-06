package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.SmsMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.body.SmsMessageBody;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 短信消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RefreshScope
public class SmsMessageSender extends AbstractMessageSender<SmsMessageBody, SmsMessage> {

    public static final String DEFAULT_QUEUE_NAME = "message.sms.queue";

    private static final String DEFAULT_TYPE = "sms";

    @Autowired
    private MessageService messageService;

    @Autowired
    private List<SmsChannelSender> smsChannelSenderList;

    /**
     * 渠道商
     */
    @Value("${message.sms.channel}")
    private String channel;

    /**
     * 最大重试次数
     */
    @Value("${message.sms.max-retry-count:3}")
    private Integer maxRetryCount;

    /**
     * 分配数量值（如果多消息时，多少个一批做消息发送）
     */
    @Value("${message.sms.number-of-batch:50}")
    private Integer numberOfBatch;

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
            )
    )
    public void sendSms(@Payload List<Integer> data,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        channel.basicAck(tag, false);

        data.forEach(this::send);


    }

    @Transactional(rollbackFor = Exception.class)
    public void send(Integer id) {
        SmsMessage entity = messageService.getSmsMessage(id);
        SmsChannelSender smsChannelSender = getSmsChannelSender(this.channel);

        entity.setLastSendTime(new Date());
        entity.setChannel(smsChannelSender.getType());

        try {

            RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity, String.format("%s:%s", restResult.getMessage(), restResult.getData()));
            } else {
                ExecuteStatus.failure(entity, restResult.getMessage());
            }

        } catch (Exception e) {
            log.error("发送短信失败", e);
            ExecuteStatus.failure(entity, e.getMessage());
        }

        retry(entity);

        messageService.saveSmsMessage(entity);

        updateBatchMessage(entity);
    }

    /**
     * 获取发送短信的渠道发送者
     *
     * @param channel 渠道类型
     * @return 短信渠道发送者
     */
    private SmsChannelSender getSmsChannelSender(String channel) {
        return smsChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的短信渠道支持"));
    }

    @Override
    protected int getNumberOfBatch() {
        return numberOfBatch;
    }

    @Override
    protected void send(List<SmsMessage> entities) {

        entities.forEach(e -> messageService.saveSmsMessage(e));
        super.send(entities);

    }

    @Override
    protected String getMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
    }

    @Override
    protected List<SmsMessage> createSendEntity(List<SmsMessageBody> result) {
        return result.stream().flatMap(this::createSmsMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过短信消息 body 构造短信消息并保存信息
     *
     * @param body 短信消息 body
     * @return 短信消息流
     */
    private Stream<SmsMessage> createSmsMessageEntity(SmsMessageBody body) {

        List<SmsMessage> result = new LinkedList<>();

        if (body.getPhoneNumbers().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[phone_nen]", "true");
            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationService.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                SmsMessage entity = ofEntity(body);
                entity.setPhoneNumber(user.get("phone").toString());

                result.add(entity);
            }

        } else {
            for (String phoneNumber : body.getPhoneNumbers()) {

                SmsMessage entity = ofEntity(body);
                entity.setPhoneNumber(phoneNumber);

                result.add(entity);
            }
        }

        return result.stream();
    }

    /**
     * 创建站内信消息实体
     *
     * @param body 站内信消息 body
     * @return 站内信消息实体
     */
    private SmsMessage ofEntity(SmsMessageBody body) {
        SmsMessage entity = Casts.of(body, SmsMessage.class);

        entity.setMaxRetryCount(maxRetryCount);

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
