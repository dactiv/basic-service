package com.fuyu.basic.commons.retry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 可重试的对象实现
 *
 * @author maurice.chen
 */
public interface Retryable {

    int DEFAULT_POW_INTERVAL_TIME = 5000;

    /**
     * 获取当前重试次数
     *
     * @return 重试次数
     */
    Integer getRetryCount();

    /**
     * 设置当前重试次数
     *
     * @return 重试次数
     */
    Integer getMaxRetryCount();

    /**
     * 获取下一次重试时间（毫秒为单位）
     *
     * @return 时间（毫秒为单位）
     */
    default Integer getNextRetryTimeInMillisecond(){
        return DEFAULT_POW_INTERVAL_TIME;
    }

    /**
     * 获取下一次间隔时间
     *
     * @return 间隔时间戳（毫秒为单位）
     */
    default Integer getNextIntervalTime() {

        return BigDecimal
                .valueOf(getRetryCount())
                .pow(getRetryCount())
                .multiply(BigDecimal.valueOf(getNextRetryTimeInMillisecond()))
                .intValue();
    }

    /**
     * 获取下一次重试时间
     *
     * @return 重试时间
     */
    default LocalDateTime getNextRetryTime() {
        return LocalDateTime.now().plus(getNextIntervalTime(), ChronoUnit.MILLIS);
    }

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    default boolean isRetry() {
        return getRetryCount() < getMaxRetryCount();
    }
}
