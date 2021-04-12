package com.fuyu.basic.support.crypto.access;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 可过期的 token
 *
 * @author maurice
 */
public interface ExpirationToken extends AccessToken {

    /**
     * 默认超时时间
     */
    int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 1800;

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    LocalDateTime getCreationTime();

    /**
     * 设置最后访问时间
     *
     * @param lastAccessedTime 最后访问时间
     */
    void setLastAccessedTime(LocalDateTime lastAccessedTime);

    /**
     * 获取最后访问时间
     *
     * @return 最后访问时间
     */
    LocalDateTime getLastAccessedTime();

    /**
     * 设置最大超时时间
     *
     * @param interval 时间
     */
    void setMaxInactiveInterval(Duration interval);

    /**
     * 获取最大超时时间
     *
     * @return 持续时间
     */
    Duration getMaxInactiveInterval();

    /**
     * 判断是否超时
     *
     * @return true 为是，否则 false
     */
    boolean isExpired();

    /**
     * 获取实际超时时间
     *
     * @return 实际超时时间
     */
    LocalDateTime getExpirationTime();
}
