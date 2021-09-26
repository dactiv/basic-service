package com.github.dactiv.basic.socket.client.holder.Interceptor;

import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.entity.SocketResult;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Objects;

/**
 *
 * spring mvc 拦截器的 socket 结果集持有者实现
 *
 * @author maurice.chen
 */
public class SocketMessageInterceptor implements MethodInterceptor {

    private final SocketClientTemplate socketClientTemplate;

    public SocketMessageInterceptor(SocketClientTemplate socketClientTemplate) {
        this.socketClientTemplate = socketClientTemplate;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        SocketMessage message = AnnotationUtils.findAnnotation(invocation.getMethod(), SocketMessage.class);

        if (Objects.isNull(message)) {
            return invocation.proceed();
        }

        SocketResultHolder.create();

        try {

            Object returnValue = invocation.proceed();

            SocketResult socketResult = SocketResultHolder.get();

            if (StringUtils.isNotEmpty(message.value())) {
                FilterResultHolder.set(message.value());
            }

            socketClientTemplate.asyncSendSocketResult(socketResult);

            return returnValue;
        } finally {
            SocketResultHolder.clear();
            FilterResultHolder.clear();
        }
    }
}
