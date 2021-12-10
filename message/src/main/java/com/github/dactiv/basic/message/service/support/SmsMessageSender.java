package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.message.config.sms.SmsConfig;
import com.github.dactiv.basic.message.domain.body.SmsMessageBody;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.basic.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.basic.message.service.SmsMessageService;
import com.github.dactiv.basic.message.service.basic.BatchMessageSender;
import com.github.dactiv.basic.message.service.support.sms.SmsChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
public class SmsMessageSender extends BatchMessageSender<SmsMessageBody, SmsMessageEntity> {

    public static final String DEFAULT_QUEUE_NAME = "message.sms.queue";

    private static final String DEFAULT_TYPE = "sms";

    private final SmsMessageService smsMessageService;

    private final List<SmsChannelSender> smsChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final SmsMessageSender oneself;

    private final SmsConfig config;

    public SmsMessageSender(SmsMessageService smsMessageService,
                            List<SmsChannelSender> smsChannelSenderList,
                            AmqpTemplate amqpTemplate,
                            SmsMessageSender oneself,
                            SmsConfig config) {
        this.smsMessageService = smsMessageService;
        this.smsChannelSenderList = smsChannelSenderList;
        this.amqpTemplate = amqpTemplate;
        this.oneself = oneself;
        this.config = config;
    }

    /**
     * 发送短信
     *
     * @param id      短信实体 id
     * @param channel 频道信息
     * @param tag     ack 值
     *
     * @throws Exception 发送失败或确认 ack 错误时抛出。
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void sendSms(@Payload Integer id,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        SmsMessageEntity entity = sendSms(id);

        if (ExecuteStatus.Retrying.equals(entity.getExecuteStatus()) && entity.getRetryCount() < getMaxRetryCount()) {
            throw new SystemException(entity.getException());
        }

        channel.basicAck(tag, false);
    }

    /**
     * 发送短信
     * å
     *
     * @param id 短信实体 id
     */
    @Transactional(rollbackFor = Exception.class)
    public SmsMessageEntity sendSms(Integer id) {

        SmsMessageEntity entity = smsMessageService.get(id);

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        SmsChannelSender smsChannelSender = getSmsChannelSender(config.getChannel());

        entity.setLastSendTime(new Date());
        entity.setChannel(smsChannelSender.getType());
        entity.setRetryCount(entity.getRetryCount() + 1);

        try {

            RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity);
            } else if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, restResult.getMessage());
            } else {
                entity.setExecuteStatus(ExecuteStatus.Retrying);
            }

        } catch (Exception e) {
            log.error("发送短信失败", e);
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                entity.setExecuteStatus(ExecuteStatus.Retrying);
            }
        }

        smsMessageService.save(entity);

        if (Objects.nonNull(entity.getBatchId())) {
            oneself.updateBatchMessage(entity);
        }

        return entity;
    }

    /**
     * 获取发送短信的渠道发送者
     *
     * @param channel 渠道类型
     *
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
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<SmsMessageEntity> content) {
        smsMessageService.save(content);
        return true;
    }

    @Override
    protected RestResult<Object> send(List<SmsMessageEntity> entities) {
        entities
                .stream()
                .map(BasicMessageEntity::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(Constants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条短信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SmsMessageEntity> getBatchMessageBodyContent(List<SmsMessageBody> result) {
        return result.stream().flatMap(this::createSmsMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过短信消息 body 构造短信消息并保存信息
     *
     * @param body 短信消息 body
     *
     * @return 短信消息流
     */
    private Stream<SmsMessageEntity> createSmsMessageEntity(SmsMessageBody body) {

        List<SmsMessageEntity> result = new LinkedList<>();

        if (body.getPhoneNumbers().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[phone_nen]", "true");
            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationFeignClient.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                SmsMessageEntity entity = Casts.of(body, SmsMessageEntity.class);
                entity.setPhoneNumber(user.get("phone").toString());

                result.add(entity);
            }

        } else {
            for (String phoneNumber : body.getPhoneNumbers()) {

                SmsMessageEntity entity = Casts.of(body, SmsMessageEntity.class);
                entity.setPhoneNumber(phoneNumber);

                result.add(entity);
            }
        }

        return result.stream();
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
