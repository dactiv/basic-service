package com.fuyu.basic.support.security.concurrent;

import com.fuyu.basic.support.security.concurrent.annotation.ConcurrentProcess;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 并发拦截器
 *
 * @author maurice
 */
public class ConcurrentInterceptor implements MethodInterceptor {

    /**
     * 默认的并发 key 前缀
     */
    private static final String DEFAULT_CONCURRENT_KEY_PREFIX = "redis:concurrent:";

    private final RedissonClient redissonClient;
    /**
     * 并发 key 前缀
     */
    private String concurrentKeyPrefix = DEFAULT_CONCURRENT_KEY_PREFIX;

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private SpelExpressionParser parser = new SpelExpressionParser();

    public ConcurrentInterceptor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public ConcurrentInterceptor(RedissonClient redissonClient, String concurrentKeyPrefix) {
        this.redissonClient = redissonClient;
        this.concurrentKeyPrefix = concurrentKeyPrefix;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        ConcurrentProcess concurrentProcess = AnnotationUtils.findAnnotation(invocation.getMethod(), ConcurrentProcess.class);

        if (Objects.isNull(concurrentProcess)) {
            return invocation.proceed();
        }

        String key = concurrentProcess.value();

        if (StringUtils.isEmpty(key)) {
            throw new ConcurrentException("并发处理的 key 不能为空");
        }

        String concurrentKey = getConcurrentKey(key, invocation);

        RLock lock = redissonClient.getLock(concurrentKey);

        boolean tryLock;

        long waitTime = concurrentProcess.waitTime();

        if (waitTime <= 0) {
            tryLock = lock.tryLock();
        } else {
            tryLock = lock.tryLock(waitTime, concurrentProcess.leaseTime(), concurrentProcess.unit());
        }

        if (tryLock) {
            try {
                return invocation.proceed();
            }  finally {
                lock.unlock();
            }
        }

        throw new ConcurrentException(concurrentProcess.exceptionMessage());

    }

    private String getConcurrentKey(String key, MethodInvocation invocation) {

        try {

            String[] parameterNames = parameterNameDiscoverer.getParameterNames(invocation.getMethod());

            EvaluationContext evaluationContext = new StandardEvaluationContext(invocation.getThis());

            for (int i = 0; i < (parameterNames != null ? parameterNames.length : 0); i++) {
                evaluationContext.setVariable(parameterNames[i], invocation.getArguments()[i]);
            }

            return concurrentKeyPrefix + parser.parseExpression(key).getValue(evaluationContext, String.class);
        } catch (Exception e) {
            return concurrentKeyPrefix + key;
        }

    }

}
