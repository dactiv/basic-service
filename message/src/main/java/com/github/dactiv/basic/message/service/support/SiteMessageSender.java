package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.Attachment;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.message.service.support.body.EmailMessageBody;
import com.github.dactiv.basic.message.service.support.body.SiteMessageBody;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Slf4j
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

    /**
     * 分配数量值（如果多消息时，多少个一批做消息发送）
     */
    @Value("${message.site.number-of-batch:50}")
    private Integer numberOfBatch;

    @Override
    protected String getRetryMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, delayed = "true")
            )
    )
    public void sendSiteMessage(@Payload List<SiteMessage> data,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        channel.basicAck(tag, false);

        data.forEach(this::send);

    }

    @Transactional(rollbackFor = Exception.class)
    public void send(SiteMessage entity) {

        SiteMessageChannelSender siteMessageChannelSender = getSiteMessageChannelSender(this.channel);

        entity.setLastSendTime(new Date());
        entity.setChannel(siteMessageChannelSender.getType());

        try {

            RestResult<Map<String, Object>> restResult = siteMessageChannelSender.sendSiteMessage(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity,String.format("%s:%s", restResult.getMessage(), restResult.getData()));
            } else {
                ExecuteStatus.failure(entity, restResult.getMessage());
            }

        } catch (Exception e) {
            log.error("发送站内信失败", e);
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
    protected int getNumberOfBatch() {
        return numberOfBatch;
    }

    @Override
    protected void send(List<SiteMessage> entities) {

        entities.forEach(e -> attachmentMessageService.saveSiteMessage(e));

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entities);
    }

    @Override
    protected String getMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
    }

    @Override
    protected List<SiteMessage> createSendEntity(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过站内信消息 body 构造站内信消息并保存信息
     *
     * @param body 站内信消息 body
     *
     * @return 邮件消息流
     */
    private Stream<SiteMessage> createSiteMessageEntity(SiteMessageBody body) {

        List<SiteMessage> result = new LinkedList<>();

        if (body.getToUserIds().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationService.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                SiteMessage entity = ofEntity(body);
                entity.setToUserId(Casts.cast(user.get(IdEntity.ID_FIELD_NAME), Integer.class));

                result.add(entity);
            }

        } else {

            for (String userId : body.getToUserIds()) {

                SiteMessage entity = ofEntity(body);
                entity.setToUserId(NumberUtils.toInt(userId));

                result.add(entity);
            }
        }

        return result.stream();
    }

    /**
     * 创建站内信消息实体
     *
     * @param body 站内信消息 body
     *
     * @return 站内信消息实体
     */
    private SiteMessage ofEntity(SiteMessageBody body) {
        SiteMessage entity = Casts.of(body, SiteMessage.class);

        entity.setMaxRetryCount(maxRetryCount);

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setHasAttachment(YesOrNo.Yes.getValue());
            body.getAttachmentList().forEach(a -> entity.getAttachmentList().add(Casts.of(a, Attachment.class)));
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

}
