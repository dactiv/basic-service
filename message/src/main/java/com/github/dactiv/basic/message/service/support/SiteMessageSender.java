package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.message.config.site.SiteConfig;
import com.github.dactiv.basic.message.domain.body.SiteMessageBody;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.basic.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.basic.message.service.SiteMessageService;
import com.github.dactiv.basic.message.service.basic.BatchMessageSender;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.idempotent.ConcurrentProperties;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
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
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ????????????????????????
 *
 * @author maurice
 */
@Slf4j
@Component
public class SiteMessageSender extends BatchMessageSender<SiteMessageBody, SiteMessageEntity> implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "message.site.queue";

    /**
     * ?????????????????????
     */
    private static final String DEFAULT_TYPE = "site";

    private final SiteMessageService siteMessageService;

    private final List<SiteMessageChannelSender> siteMessageChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final SiteConfig config;

    public SiteMessageSender(SiteMessageService siteMessageService,
                             List<SiteMessageChannelSender> siteMessageChannelSenderList,
                             AmqpTemplate amqpTemplate,
                             ConcurrentInterceptor concurrentInterceptor,
                             SiteConfig config) {

        this.siteMessageService = siteMessageService;
        this.siteMessageChannelSenderList = siteMessageChannelSenderList;
        this.amqpTemplate = amqpTemplate;
        this.concurrentInterceptor = concurrentInterceptor;
        this.config = config;
    }

    /**
     * ???????????????
     *
     * @param id      ??????????????? id
     * @param channel ????????????
     * @param tag     ack ???
     *
     * @throws Exception ????????????????????? ack ??????????????????
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void sendSiteMessage(@Payload Integer id,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        SiteMessageEntity entity = sendSiteMessage(id);

        if (ExecuteStatus.Retrying.equals(entity.getExecuteStatus()) && entity.getRetryCount() < getMaxRetryCount()) {
            throw new SystemException(entity.getException());
        }

        channel.basicAck(tag, false);

    }

    /**
     * ???????????????
     *
     * @param id ??????????????? id
     */
    @Transactional(rollbackFor = Exception.class)
    public SiteMessageEntity sendSiteMessage(Integer id) {

        SiteMessageEntity entity = siteMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        SiteMessageChannelSender siteMessageChannelSender = getSiteMessageChannelSender(config.getChannel());

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
            log.error("?????????????????????", e);
            ExecuteStatus.failure(entity, e.getMessage());
        }

        siteMessageService.save(entity);

        if (Objects.nonNull(entity.getBatchId())) {
            ConcurrentProperties properties = config.getBatchUpdateConcurrent().ofSuffix(entity.getBatchId());
            concurrentInterceptor.invoke(properties, () -> updateBatchMessage(entity));
        }

        return entity;
    }

    /**
     * ????????????????????????????????????
     *
     * @param channel ????????????
     *
     * @return ??????????????????????????????
     */
    private SiteMessageChannelSender getSiteMessageChannelSender(String channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("??????????????????[" + channel + "]???????????????????????????"));
    }

    /**
     * ????????????????????? body ????????????????????????????????????
     *
     * @param body ??????????????? body
     *
     * @return ???????????????
     */
    private Stream<SiteMessageEntity> createSiteMessageEntity(SiteMessageBody body) {

        List<SiteMessageEntity> result = new LinkedList<>();

        if (body.getToUserIds().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationFeignClient.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                SiteMessageEntity entity = ofEntity(body);
                entity.setToUserId(Casts.cast(user.get(IdEntity.ID_FIELD_NAME), Integer.class));

                result.add(entity);
            }

        } else {

            for (String userId : body.getToUserIds()) {

                SiteMessageEntity entity = ofEntity(body);
                entity.setToUserId(NumberUtils.toInt(userId));

                result.add(entity);
            }
        }

        return result.stream();
    }

    /**
     * ???????????????????????????
     *
     * @param body ??????????????? body
     *
     * @return ?????????????????????
     */
    private SiteMessageEntity ofEntity(SiteMessageBody body) {
        SiteMessageEntity entity = Casts.of(body, SiteMessageEntity.class, "attachmentList");

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setHasAttachment(YesOrNo.Yes);
            body.getAttachmentList().forEach(a -> entity.getAttachmentList().add(Casts.of(a, AttachmentEntity.class)));
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<SiteMessageEntity> content) {
        siteMessageService.save(content);
        return true;
    }

    @Override
    protected RestResult<Object> send(List<SiteMessageEntity> entities) {
        entities
                .stream()
                .map(BasicMessageEntity::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "?????? " + entities.size() + " ????????????????????????",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SiteMessageEntity> getBatchMessageBodyContent(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessageEntity).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(getMessageType())));
    }
}
