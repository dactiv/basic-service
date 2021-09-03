package com.github.dactiv.basic.message.service;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.AttachmentMessage;
import com.github.dactiv.basic.message.entity.BasicMessage;
import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.enumerate.AttachmentType;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.retry.Retryable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象的消息发送者实现，主要是构建和验证发型实体得到真正的发送者实体而实现的一个抽象类
 *
 * @param <T> 消息的请求数据泛型实体类型
 * @param <S> 消息的发送数据泛型实体类型
 *
 * @author maurice
 */
@Slf4j
public abstract class AbstractMessageSender<T extends BasicMessage, S extends NumberIdEntity<Integer>> implements MessageSender {

    private static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    public static final String DEFAULT_ALL_USER_KEY = "ALL_USER";

    @Autowired
    protected AmqpTemplate amqpTemplate;

    @Autowired
    private MessageService messageService;

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    /**
     * 文件管理服务
     */
    @Autowired
    protected FileManagerService fileManagerService;

    /**
     * 会员用户服务
     */
    @Autowired
    protected AuthenticationService authenticationService;

    /**
     * 线程池，用于批量发送消息时候异步使用。
     */
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 请求对象的数据实体类型
     */
    private final Class<T> bodyEntityClass;

    /**
     * 发送对象的数据实体类型
     */
    private final Class<S> sendEntityClass;

    /**
     * 批量消息对应的附件缓存 map
     */
    protected final Map<Integer, Map<String, byte[]>> attachmentCache = new LinkedHashMap<>();

    public AbstractMessageSender() {
        this.bodyEntityClass = ReflectionUtils.getGenericClass(this, 0);
        this.sendEntityClass = ReflectionUtils.getGenericClass(this, 1);
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
                T entity = bindAndValidate(m);
                result.add(entity);
            }

        } else {
            T entity = bindAndValidate(request);
            result.add(entity);
        }

        return sendMessage(result);
    }

    /**
     * 发送消息
     *
     * @param result body 集合
     *
     * @return reset 结果集
     */
    @Transactional(rollbackFor = Exception.class)
    protected RestResult<Map<String, Object>> sendMessage(List<T> result) {
        // 构造发送消息结果集，用于 send 发送数据使用
        List<S> sendResult = createSendEntity(result);

        List<BatchMessage.Body> bodyList = sendResult
                .stream()
                .filter(s -> BatchMessage.Body.class.isAssignableFrom(s.getClass()))
                .map(s -> Casts.cast(s, BatchMessage.Body.class))
                .collect(Collectors.toList());

        RestResult<Map<String, Object>> restResult;

        // 如果发送消息的结果集大于 0，构造批量订单
        if (bodyList.size() > 1) {

            BatchMessage batchMessage = new BatchMessage();

            batchMessage.setCount(sendResult.size());
            batchMessage.setSendingNumber(batchMessage.getCount());

            AttachmentType attachmentType = AttachmentType.valueOf(bodyEntityClass);
            batchMessage.setType(attachmentType.getValue());

            messageService.saveBatchMessage(batchMessage);

            bodyList.forEach(r -> r.setBatchId(batchMessage.getId()));

            onBatchMessageCreate(batchMessage, result, sendResult);

            Map<String, Object> data = Map.of(
                    DEFAULT_BATCH_MESSAGE_ID_KEY, batchMessage.getId(),
                    DEFAULT_MESSAGE_COUNT_KEY, sendResult.size()
            );

            threadPoolTaskExecutor.execute(() -> send(sendResult));

            restResult = RestResult.ofSuccess(
                    "发送" + sendResult.size() + "条 [" + getMessageType() + "] 消息成功",
                    data
            );
        } else {
            send(sendResult);

            Map<String, Object> data = Map.of(
                    IdEntity.ID_FIELD_NAME,
                    sendResult
                            .stream()
                            .map(s -> Casts.cast(s, sendEntityClass))
                            .map(NumberIdEntity::getId)
                            .findFirst()
                            .orElseThrow(() -> new SystemException("找不到类型为 [" + sendEntityClass + "] 的 id 值"))
            );

            restResult = RestResult.ofSuccess("发送 [" + getMessageType() + "] 消息成功", data);
        }

        log.info("发送类型为: [" + getMessageType() + "] 的消息,响应信息为:" + Casts.convertValue(restResult, Map.class));

        return restResult;
    }

    /**
     * 绑定并验证请求数据
     *
     * @param value 请求参数
     *
     * @return body
     *
     * @throws BindException 验证数据错误时抛出
     */
    private T bindAndValidate(Map<String, Object> value) throws BindException {
        T entity = Casts.convertValue(value, bodyEntityClass);
        WebDataBinder binder = new WebDataBinder(entity, entity.getClass().getSimpleName());

        if (validator != null) {

            binder.setValidator(validator);
            binder.validate();

            if (binder.getBindingResult().hasErrors()) {
                throw new BindException(binder.getBindingResult());
            }

        }

        postBindValue(entity, value);

        return entity;
    }

    /**
     * 绑定值后的处理
     *
     * @param entity 消息的请求数据泛型实体
     * @param value 被绑定的数据值（请求参数）
     */
    protected void postBindValue(T entity, Map<String, Object> value) {

    }

    /**
     * 重试
     *
     * @param entity 批量消息接口实现类
     */
    // FIXME 这里应该支持批量重试
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
     * 更新批量消息
     *
     * @param body 批量消息接口实现类
     */
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
                .filter(t -> AttachmentMessage.class.isAssignableFrom(t.getClass()))
                .map(t -> Casts.cast(t, AttachmentMessage.class))
                .flatMap(t -> t.getAttachmentList().stream())
                .filter(a -> !map.containsKey(a.getName()))
                .map(a -> fileManagerService.get(a.getMeta().get(FileManagerService.DEFAULT_BUCKET_NAME).toString(), a.getName()))
                .forEach(r -> map.put(r.getHeaders().getContentDisposition().getFilename(), r.getBody()));

    }

    /**
     * 获取分批 map
     *
     * @param sendList 要发送的数据泛型实体
     *
     * @return 分批 map
     */
    protected Map<Integer,List<Integer>> getBatchMap(List<S> sendList) {

        List<S> temps = new LinkedList<>(sendList);

        Map<Integer, List<Integer>> result = new HashMap<>();

        for (int offset = 0; temps.size() > getNumberOfBatch(); offset++) {
            S entity = temps.iterator().next();

            List<Integer> list;

            if (offset % getNumberOfBatch() == 0) {
                int number = result.keySet().size() + 1;
                list = result.computeIfAbsent(number, k -> new LinkedList<>());
            } else {
                list = result.get(result.keySet().size());
            }

            list.add(entity.getId());

            temps.remove(entity);
        }

        List<Integer> last = result.computeIfAbsent(
                result.keySet().size() + 1,
                k -> new LinkedList<>()
        );

        last.addAll(temps.stream().map(NumberIdEntity::getId).collect(Collectors.toList()));

        return result;
    }

    /**
     * 获取分配总数，每一批多少个消息
     *
     * @return 分配总数
     */
    protected abstract int getNumberOfBatch();

    /**
     * 发送消息
     *
     * @param entities 消息实体集合
     */
    protected void send(List<S> entities) {
        getBatchMap(entities)
                .forEach((key, value) ->
                        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, getMessageQueueName(), value));
    }

    /**
     * 获取发送消息队列名称
     *
     * @return 发送消息队列名称
     */
    protected abstract String getMessageQueueName();

    /**
     * 创建要发送的实体
     *
     * @param result request 请求构造的实体集合
     *
     * @return 要发送的实体集合
     */
    protected abstract List<S> createSendEntity(List<T> result);

    /**
     * 获取重试消息队列名称
     *
     * @return 重试消息队列名称
     */
    protected abstract String getRetryMessageQueueName();

}
