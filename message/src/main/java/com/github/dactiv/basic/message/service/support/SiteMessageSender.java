package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Component
public class SiteMessageSender extends AbstractMessageSender<SiteMessage> {

    public static final String DEFAULT_QUEUE_NAME = "message.site.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "siteMessage";

    @Autowired
    private MessageService messageService;

    @Autowired
    private List<SiteMessageChannelSender> siteMessageChannelSenderList;

    /**
     * 渠道商
     */
    @Value("${spring.site.message.channel}")
    private String channel;

    /**
     * 最大重试次数
     */
    @Value("${spring.site.message.max-retry-count:3}")
    private Integer maxRetryCount;

    @Override
    protected void afterBindValueSetting(SiteMessage entity, Map<String, Object> value) {
        entity.setMaxRetryCount(maxRetryCount);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, delayed = "true")
            ),
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void sendSiteMessage(@Payload List<SiteMessage> data,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        channel.basicAck(tag, false);

        data.forEach(entity -> {

            entity.setLastSendTime(new Date());

            SiteMessageChannelSender siteMessageChannelSender = getSiteMessageChannelSender(this.channel);

            entity.setChannel(siteMessageChannelSender.getType());

            try {

                RestResult<Map<String, Object>> restResult = siteMessageChannelSender.sendSiteMessage(entity);

                if (restResult.getStatus() == HttpStatus.OK.value()) {
                    ExecuteStatus.success(entity,String.format("%s:%s", restResult.getMessage(), restResult.getData()));
                } else {
                    ExecuteStatus.failure(entity, restResult.getMessage());
                }

            } catch (Exception e) {
                ExecuteStatus.failure(entity, e.getMessage());
            }

            if (ExecuteStatus.Failure.getValue().equals(entity.getStatus()) && entity.isRetry()) {

                entity.setRetryCount(entity.getRetryCount() + 1);
                messageService.saveSiteMessage(entity);

                amqpTemplate.convertAndSend(
                        RabbitmqConfig.DEFAULT_DELAY_EXCHANGE,
                        DEFAULT_QUEUE_NAME,
                        Collections.singletonList(entity),
                        message -> {
                            message.getMessageProperties().setDelay(entity.getNextIntervalTime());
                            return message;
                        });

            } else {

                messageService.saveSiteMessage(entity);

            }
        });

    }

    private SiteMessageChannelSender getSiteMessageChannelSender(String channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的消息推送渠道支持"));
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<SiteMessage> entity) {

        messageService.saveSiteMessages(entity);

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entity);

        return RestResult.of("发送站内信成功");
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

}
