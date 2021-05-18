package com.github.dactiv.basic.message.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 抽象的消息发送者实现，主要是构建和验证发型实体得到真正的发送者实体而实现的一个抽象类
 *
 * @param <T> 消息泛型实体
 * @author maurice
 */
public abstract class AbstractMessageSender<T> implements MessageSender {

    private static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    @Autowired
    protected AmqpTemplate amqpTemplate;

    private final Class<T> entityClass;

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    public AbstractMessageSender() {
        this.entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public RestResult<Map<String, Object>> send(Map<String, Object> request) throws Exception {

        List<T> result = new LinkedList<>();

        if (request.containsKey(DEFAULT_BATCH_MESSAGE_KEY)) {
            List<Map<String, Object>> messages = Casts.cast(request.get(DEFAULT_BATCH_MESSAGE_KEY), List.class);
            for (Map<String, Object> m : messages) {
                T entity = entityClass.newInstance();
                bindAndValidate(entity, m);
                result.add(entity);
            }
        } else {
            T entity = entityClass.newInstance();
            bindAndValidate(entity, request);
            result.add(entity);
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

    /**
     * 发送消息
     *
     * @param entity 消息实体
     * @return rest 结果集
     */
    protected abstract RestResult<Map<String, Object>> send(List<T> entity);

}
