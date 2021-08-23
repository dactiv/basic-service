package com.github.dactiv.basic.message.service;

import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.enumerate.AttachmentType;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.retry.Retryable;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.*;

/**
 * 抽象的消息发送者实现，主要是构建和验证发型实体得到真正的发送者实体而实现的一个抽象类
 *
 * @param <T> 消息泛型实体
 * @author maurice
 */
public abstract class AbstractMessageSender<T extends BatchMessage.Body> implements MessageSender {

    private static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    @Autowired
    protected AmqpTemplate amqpTemplate;

    @Autowired
    private MessageService messageService;

    private final Class<T> entityClass;

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    public AbstractMessageSender() {
        this.entityClass = ReflectionUtils.getGenericClass(this.getClass().getGenericSuperclass(), 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Map<String, Object>> send(Map<String, Object> request) throws Exception {

        List<T> result = new LinkedList<>();

        if (request.containsKey(DEFAULT_BATCH_MESSAGE_KEY)) {
            List<Map<String, Object>> messages = Casts.cast(request.get(DEFAULT_BATCH_MESSAGE_KEY), List.class);
            for (Map<String, Object> m : messages) {
                T entity = ClassUtils.newInstance(entityClass);
                bindAndValidate(entity, m);
                result.add(entity);
            }
        } else {
            T entity = ClassUtils.newInstance(entityClass);
            bindAndValidate(entity, request);
            result.add(entity);
        }

        if (result.size() > 1 && BatchMessage.Body.class.isAssignableFrom(entityClass)) {

            BatchMessage batchMessage = new BatchMessage();

            batchMessage.setCount(result.size());
            batchMessage.setSendingNumber(batchMessage.getCount());

            AttachmentType attachmentType = AttachmentType.valueOf(entityClass);
            batchMessage.setType(attachmentType.getValue());

            messageService.saveBatchMessage(batchMessage);

            result.forEach(r -> r.setBatchId(batchMessage.getId()));
        }

        return send(result);
    }

    private void bindAndValidate(T entity, Map<String, Object> value) throws BindException {
        WebDataBinder binder = new WebDataBinder(entity, entity.getClass().getSimpleName());
        MutablePropertyValues values = new MutablePropertyValues(value);
        binder.bind(values);

        if (validator != null) {

            binder.setValidator(validator);
            binder.validate();

            if (binder.getBindingResult().hasErrors()) {
                throw new BindException(binder.getBindingResult());
            }

        }

        afterBindValueSetting(entity, value);
    }

    protected void afterBindValueSetting(T entity, Map<String, Object> value) {

    }

    public void retry(BatchMessage.Body entity) {

        if (!Retryable.class.isAssignableFrom(entity.getClass())) {
            return ;
        }

        Retryable retryable = Casts.cast(entity);

        if (ExecuteStatus.Failure.getValue().equals(entity.getStatus())) {

            retryable.setRetryCount(retryable.getRetryCount() + 1);

            amqpTemplate.convertAndSend(
                    RabbitmqConfig.DEFAULT_DELAY_EXCHANGE,
                    getRetryMessageQueueName(),
                    Collections.singletonList(retryable),
                    message -> {
                        message.getMessageProperties().setDelay(retryable.getNextIntervalTime());
                        return message;
                    });

        }

    }

    /**
     * 获取重试队列 MQ 名称
     *
     * @return 重试队列 MQ 名称
     */
    protected abstract String getRetryMessageQueueName();

    protected void updateBatchMessage(BatchMessage.Body body) {

        if (Objects.isNull(body.getBatchId())) {
            return ;
        }

        BatchMessage batchMessage = messageService.getBatchMessage(body.getBatchId());

        if (ExecuteStatus.Success.getValue().equals(body.getStatus())) {

            batchMessage.setSuccessNumber(batchMessage.getSuccessNumber() + 1);
            batchMessage.setSendingNumber(batchMessage.getSendingNumber() - 1);

        } else if (ExecuteStatus.Failure.getValue().equals(body.getStatus())) {

            if (Retryable.class.isAssignableFrom(body.getClass())) {

                Retryable retryable = Casts.cast(body);

                if (!retryable.isRetry()) {
                    batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
                    batchMessage.setSendingNumber(batchMessage.getSendingNumber() - 1);
                }

            } else {
                batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
                batchMessage.setSendingNumber(batchMessage.getSendingNumber() - 1);
            }

        }

        if (batchMessage.getCount().equals(batchMessage.getSuccessNumber() + batchMessage.getFailNumber())) {
            batchMessage.setStatus(ExecuteStatus.Success.getValue());
            batchMessage.setCompleteTime(new Date());
        }

        messageService.saveBatchMessage(batchMessage);

    }

    /**
     * 发送消息
     *
     * @param entity 消息实体
     * @return rest 结果集
     */
    protected abstract RestResult<Map<String, Object>> send(List<T> entity);

}
