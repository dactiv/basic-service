package com.github.dactiv.basic.socket.client.holder;

import com.github.dactiv.basic.socket.client.holder.Interceptor.SocketMessageInterceptor;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * socket 消息注解的切面实现
 *
 * @author maurice
 */
public class SocketMessagePointcutAdvisor extends AbstractPointcutAdvisor {

    private final SocketMessageInterceptor socketMessageInterceptor;

    public SocketMessagePointcutAdvisor(SocketMessageInterceptor socketMessageInterceptor) {
        this.socketMessageInterceptor = socketMessageInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.isAnnotationPresent(SocketMessage.class) || targetClass.isAnnotationPresent(SocketMessage.class);
            }

        };
    }

    @Override
    public Advice getAdvice() {
        return socketMessageInterceptor;
    }
}
