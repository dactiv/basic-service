package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.message.service.support.body.SiteMessageBody;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.Casts;
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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Component
@RefreshScope
public class SiteMessageSender extends AbstractMessageSender<SiteMessageBody, SiteMessage> {

    public static final String DEFAULT_QUEUE_NAME = "message.site.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "siteMessage";

    @Autowired
    private AttachmentMessageService attachmentMessageService;

    @Autowired
    private List<SiteMessageChannelSender> siteMessageChannelSenderList;

    /**
     * 渠道商
     */
    @Value("${message.site.channel}")
    private String channel;

    /**
     * 最大重试次数
     */
    @Value("${message.site.max-retry-count:3}")
    private Integer maxRetryCount;

    @Override
    protected String getRetryMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
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

        data.forEach(this::send);

    }

    private void send(SiteMessage entity) {

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

        retry(entity);

        attachmentMessageService.saveSiteMessage(entity);

        updateBatchMessage(entity);
    }

    private SiteMessageChannelSender getSiteMessageChannelSender(String channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的消息推送渠道支持"));
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<SiteMessage> entities) {

        entities.forEach(e -> attachmentMessageService.saveSiteMessage(e));

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entities);

        return RestResult.ofSuccess("发送站内信成功", Map.of(DEFAULT_MESSAGE_COUNT_KEY, entities.size()));
    }

    @Override
    protected List<SiteMessage> createSendEntity(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createAndSaveEmailMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过站内信消息 body 构造站内信消息并保存信息
     *
     * @param body 站内信消息 body
     *
     * @return 邮件消息流
     */
    private Stream<SiteMessage> createAndSaveEmailMessageEntity(SiteMessageBody body) {

        List<SiteMessage> result = new LinkedList<>();

        for (Integer userId : body.getToUserIds()) {

            SiteMessage entity = Casts.of(body, SiteMessage.class);

            entity.setFromUserId(userId);
            entity.setMaxRetryCount(maxRetryCount);

            result.add(entity);
        }

        return result.stream();
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

}
