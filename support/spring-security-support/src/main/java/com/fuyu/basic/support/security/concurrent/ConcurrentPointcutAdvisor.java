package com.fuyu.basic.support.security.concurrent;

import com.fuyu.basic.support.security.concurrent.annotation.ConcurrentProcess;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * 并发处理的饥饿且实现
 *
 * @author maurice
 */
public class ConcurrentPointcutAdvisor extends AbstractPointcutAdvisor {

    private final ConcurrentInterceptor concurrentInterceptor;

    public ConcurrentPointcutAdvisor(ConcurrentInterceptor concurrentInterceptor) {
        this.concurrentInterceptor = concurrentInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.isAnnotationPresent(ConcurrentProcess.class) || targetClass.isAnnotationPresent(ConcurrentProcess.class);
            }

        };
    }

    @Override
    public Advice getAdvice() {
        return concurrentInterceptor;
    }
}
