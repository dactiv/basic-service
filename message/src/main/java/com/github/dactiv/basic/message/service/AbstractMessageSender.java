package com.github.dactiv.basic.message.service;

import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.AttachmentMessage;
import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.enumerate.AttachmentType;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public abstract class AbstractMessageSender<T extends BatchMessage.Body, S extends BatchMessage.Body> implements MessageSender {

    private static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    @Autowired
    protected AmqpTemplate amqpTemplate;

    @Autowired
    private MessageService messageService;

    /**
     * 批量消息对应的附件缓存 map
     */
    protected final Map<Integer, Map<String, byte[]>> attachmentCache = new LinkedHashMap<>();

    /**
     * 文件管理服务
     */
    @Autowired
    protected FileManagerService fileManagerService;

    /**
     * 请求对象的数据实体类型
     */
    private final Class<T> entityClass;

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    public AbstractMessageSender() {
        this.entityClass = ReflectionUtils.getGenericClass(this, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Map<String, Object>> send(Map<String, Object> request) throws Exception {

        List<T> result = new LinkedList<>();

        // 如果存在批量消息构造数据集合，否则构造单个实体
        if (request.containsKey(DEFAULT_BATCH_MESSAGE_KEY)) {
            //noinspection unchecked
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

        return sendMessage(result);
    }

    @Transactional(rollbackFor = Exception.class)
    protected RestResult<Map<String, Object>> sendMessage(List<T> result) {
        // 构造发送消息结果集，用于 send 发送数据使用
        List<S> sendResult = createSendEntity(result);

        Integer batchId = null;

        // 如果发送消息的结果集大于 0，构造批量订单
        if (sendResult.size() > 1 && BatchMessage.Body.class.isAssignableFrom(entityClass)) {

            BatchMessage batchMessage = new BatchMessage();

            batchMessage.setCount(result.size());
            batchMessage.setSendingNumber(batchMessage.getCount());

            AttachmentType attachmentType = AttachmentType.valueOf(entityClass);
            batchMessage.setType(attachmentType.getValue());

            messageService.saveBatchMessage(batchMessage);

            result.forEach(r -> r.setBatchId(batchMessage.getId()));

            batchId = batchMessage.getId();

            onBatchMessageCreate(batchMessage, result, sendResult);
        }

        RestResult<Map<String, Object>> restResult = send(sendResult);

        if (Objects.nonNull(batchId)) {
            restResult.getData().put(DEFAULT_BATCH_MESSAGE_ID_KEY, batchId);
        }

        log.info("发送类型为: [" + getMessageType() + "] 的消息,响应信息为:" + Casts.convertValue(restResult, Map.class));

        return restResult;
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

            onBatchMessageComplete(batchMessage);
        }

        messageService.saveBatchMessage(batchMessage);

    }

    /**
     * 当批量信息发送完成时，触发此方法。
     *
     * @param batchMessage 批量信息实体
     */
    protected void onBatchMessageComplete(BatchMessage batchMessage) {
        attachmentCache.remove(batchMessage.getId());
    }

    /**
     * 当批量信息创建完成后，触发此方法
     *
     * @param batchMessage 批量信息实体
     * @param bodyList request 传过来的 body 参数集合
     * @param sendList 通过 body 构造完成的待发送数据集合
     */
    protected void onBatchMessageCreate(BatchMessage batchMessage, List<T> bodyList,  List<S> sendList) {
        Map<String, byte[]> map = attachmentCache.computeIfAbsent(batchMessage.getId(), k -> new LinkedHashMap<>());

        bodyList
                .stream()
                .filter(t -> !AttachmentMessage.class.isAssignableFrom(t.getClass()))
                .map(t -> Casts.cast(t, AttachmentMessage.class))
                .flatMap(t -> t.getAttachmentList().stream())
                .filter(a -> !map.containsKey(a.getName()))
                .map(a -> fileManagerService.get(a.getMeta().get(FileManagerService.DEFAULT_BUCKET_NAME).toString(), a.getName()))
                .forEach(r -> map.put(r.getHeaders().getContentDisposition().getFilename(), r.getBody()));

    }

    /**
     * 发送消息
     *
     * @param entity 消息实体
     * @return rest 结果集
     */
    protected abstract RestResult<Map<String, Object>> send(List<S> entity);

    /**
     * 创建要发送的实体
     *
     * @param result request 请求构造的实体集合
     *
     * @return 要发送的实体集合
     */
    protected abstract List<S> createSendEntity(List<T> result);

    /**
     * 获取重试队列 MQ 名称
     *
     * @return 重试队列 MQ 名称
     */
    protected abstract String getRetryMessageQueueName();

}
