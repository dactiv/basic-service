package com.github.dactiv.basic.message.service.basic;

import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.basic.message.service.MessageSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 抽象的消息发送者实现，主要是构建和验证发送消息实体的一个抽象类
 *
 * @param <T> 消息的请求数据泛型实体类型
 *
 * @author maurice
 */
@Slf4j
public abstract class AbstractMessageSender<T extends BasicMessageEntity> implements MessageSender {

    private static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    /**
     * 请求对象的数据实体类型
     */
    protected final Class<T> entityClass;

    /**
     * 批量消息对应的附件缓存 map
     */
    protected final Map<Integer, Map<String, byte[]>> attachmentCache = new LinkedHashMap<>();

    public AbstractMessageSender() {
        this.entityClass = ReflectionUtils.getGenericClass(this, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Object> send(Map<String, Object> request) throws Exception {

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
    protected abstract RestResult<Object> sendMessage(List<T> result);

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
        T entity = Casts.convertValue(value, entityClass);
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
     * @param value  被绑定的数据值（请求参数）
     */
    protected void postBindValue(T entity, Map<String, Object> value) {

    }

}
