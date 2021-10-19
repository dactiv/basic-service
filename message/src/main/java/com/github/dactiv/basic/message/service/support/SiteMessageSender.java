package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.message.entity.Attachment;
import com.github.dactiv.basic.message.entity.BasicMessage;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.message.service.basic.BatchMessageSender;
import com.github.dactiv.basic.message.service.support.body.SiteMessageBody;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.minio.data.Bucket;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * 站内信消息发送者
 *
 * @author maurice
 */
@Slf4j
@Component
@RefreshScope
public class SiteMessageSender extends BatchMessageSender<SiteMessageBody, SiteMessage> implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "message.site.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "site";

    @Autowired
    private AttachmentMessageService attachmentMessageService;

    @Autowired
    private List<SiteMessageChannelSender> siteMessageChannelSenderList;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SiteMessageSender oneself;

    /**
     * 渠道商
     */
    @Value("${dactiv.message.site.channel}")
    private String channel;

    /**
     * 发送站内信
     *
     * @param id      站内信实体 id
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
    public void sendSiteMessage(@Payload Integer id,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        SiteMessage entity = sendSiteMessage(id);

        if (ExecuteStatus.Retrying.getValue().equals(entity.getStatus()) && entity.getRetryCount() < getMaxRetryCount()) {
            throw new SystemException(entity.getException());
        }

        channel.basicAck(tag, false);

    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
     */
    @Transactional(rollbackFor = Exception.class)
    public SiteMessage sendSiteMessage(Integer id) {

        SiteMessage entity = attachmentMessageService.getSiteMessage(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        SiteMessageChannelSender siteMessageChannelSender = getSiteMessageChannelSender(this.channel);

        entity.setLastSendTime(new Date());
        entity.setChannel(siteMessageChannelSender.getType());
        entity.setRetryCount(entity.getRetryCount() + 1);

        try {

            RestResult<Map<String, Object>> restResult = siteMessageChannelSender.sendSiteMessage(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity);
            } else {
                ExecuteStatus.failure(entity, restResult.getMessage());
            }

        } catch (Exception e) {
            log.error("发送站内信失败", e);
            ExecuteStatus.failure(entity, e.getMessage());
        }

        attachmentMessageService.saveSiteMessage(entity);

        if (Objects.nonNull(entity.getBatchId())) {
            oneself.updateBatchMessage(entity);
        }

        return entity;
    }

    /**
     * 获取站内信消息渠道发送者
     *
     * @param channel 渠道类型
     *
     * @return 站内信消息渠道发送者
     */
    private SiteMessageChannelSender getSiteMessageChannelSender(String channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的消息推送渠道支持"));
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
        SiteMessage entity = Casts.of(body, SiteMessage.class, "attachmentList");

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<SiteMessage> content) {
        content.forEach(e -> attachmentMessageService.saveSiteMessage(e));
        return true;
    }

    @Override
    protected RestResult<Object> send(List<SiteMessage> entities) {
        entities
                .stream()
                .map(BasicMessage::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(Constants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条站内信消息完成",
                entities.stream().map(BasicMessage::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SiteMessage> getBatchMessageBodyContent(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessageEntity).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(getMessageType())));
    }
}
