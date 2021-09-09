package com.github.dactiv.basic.message.service.basic;

import com.github.dactiv.basic.message.entity.AttachmentMessage;
import com.github.dactiv.basic.message.entity.BasicMessage;
import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.enumerate.AttachmentType;
import com.github.dactiv.basic.message.service.AuthenticationService;
import com.github.dactiv.basic.message.service.FileManagerService;
import com.github.dactiv.basic.message.service.MessageService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;

/**
 * 批量消息发送的抽象实现，用于对需要创建 tb_batch_message 记录的消息做一个统一处理
 *
 * @param <T> 批量消息数据的泛型实体类型
 * @param <S> 请求的消息数据泛型实体类型
 */
public abstract class BatchMessageSender<T extends BasicMessage, S extends BatchMessage.Body> extends AbstractMessageSender<T>{

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    public static final String DEFAULT_ALL_USER_KEY = "ALL_USER";

    @Autowired
    private MessageService messageService;

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

    protected final Class<S> sendEntityClass;

    public BatchMessageSender() {
        this.sendEntityClass = ReflectionUtils.getGenericClass(this, 1);
    }

    @Override
    protected RestResult<Object> sendMessage(List<T> result) {

        List<S> content = getBatchMessageBodyContent(result);

        Objects.requireNonNull(content, "批量消息 body 内容不能为空");

        RestResult<Object> restResult = RestResult.ofException(
                String.valueOf(HttpStatus.NO_CONTENT.value()),
                new SystemException("未知执行结果")
        );

        if (content.size() > 1) {

            BatchMessage batchMessage = new BatchMessage();

            batchMessage.setCount(content.size());
            batchMessage.setSendingNumber(batchMessage.getCount());

            AttachmentType attachmentType = AttachmentType.valueOf(entityClass);
            batchMessage.setType(attachmentType.getValue());

            messageService.saveBatchMessage(batchMessage);

            content.forEach(r -> r.setBatchId(batchMessage.getId()));

            Map<String, Object> data = Map.of(
                    DEFAULT_BATCH_MESSAGE_ID_KEY, batchMessage.getId(),
                    DEFAULT_MESSAGE_COUNT_KEY, content.size()
            );

            threadPoolTaskExecutor.execute(() -> {
                batchMessageCreated(batchMessage, result, content);
                boolean send = preSend(content);

                if (!send) {
                    send(content);
                }

            });

            restResult = RestResult.ofSuccess(
                    "发送" + content.size() + "条 [" + getMessageType() + "] 消息成功",
                    data
            );
        } else {
            boolean send = preSend(content);

            if (!send) {
                restResult = send(content);
            }
        }

        return restResult;
    }

    /**
     * 发送消息前的处理
     * @param content 批量消息内容
     * @return true 继续发送，否则 false
     */
    protected boolean preSend(List<S> content) {
        return true;
    }

    /**
     * 发送批量消息
     *
     * @param result 批量消息内容
     *
     * @return rest 结果集
     */
    protected abstract RestResult<Object> send(List<S> result);

    /**
     * 获取批量消息数据内容
     *
     * @param result 消息的请求数据泛型实体集合
     *
     * @return 批量消息数据的泛型实体集合
     */
    protected abstract List<S> getBatchMessageBodyContent(List<T> result);

    /**
     * 更新批量消息
     *
     * @param body 批量消息接口实现类
     */
    protected void updateBatchMessage(BatchMessage.Body body) {

        if (Objects.isNull(body.getBatchId())) {
            return;
        }

        BatchMessage batchMessage = messageService.getBatchMessage(body.getBatchId());

        if (ExecuteStatus.Success.getValue().equals(body.getStatus())) {
            batchMessage.setSendingNumber(batchMessage.getSendingNumber() - 1);
        } else if (ExecuteStatus.Failure.getValue().equals(body.getStatus())) {
            batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
        }

        if (batchMessage.getCount().equals(batchMessage.getSuccessNumber() + batchMessage.getFailNumber())) {
            batchMessage.setStatus(ExecuteStatus.Success.getValue());
            batchMessage.setCompleteTime(new Date());

            onBatchMessageComplete(batchMessage);
        }

        messageService.saveBatchMessage(batchMessage);

    }

    /**
     * 当批量信息创建完成后，触发此方法
     *
     * @param batchMessage 批量信息实体
     * @param bodyResult   request 传过来的 body 参数集合
     */
    protected void batchMessageCreated(BatchMessage batchMessage, List<T> bodyResult, List<S> content) {
        Map<String, byte[]> map = attachmentCache.computeIfAbsent(batchMessage.getId(), k -> new LinkedHashMap<>());

        bodyResult
                .stream()
                .filter(t -> AttachmentMessage.class.isAssignableFrom(t.getClass()))
                .map(t -> Casts.cast(t, AttachmentMessage.class))
                .flatMap(t -> t.getAttachmentList().stream())
                .filter(a -> !map.containsKey(a.getName()))
                .map(a -> fileManagerService.get(a.getMeta().get(FileManagerService.DEFAULT_BUCKET_NAME).toString(), a.getName()))
                .forEach(r -> map.put(r.getHeaders().getContentDisposition().getFilename(), r.getBody()));

    }

    /**
     * 当批量信息发送完成时，触发此方法。
     *
     * @param batchMessage 批量信息实体
     */
    protected void onBatchMessageComplete(BatchMessage batchMessage) {
        attachmentCache.remove(batchMessage.getId());
    }
}
