package com.github.dactiv.basic.captcha.service;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

/**
 * 验证码的绑定 token，主要用于让系统知道“谁”在用验证码服务，
 * 并针对每个验证码类型提供自己的构建验证码参数给前端自己通过参数信息去构建指定的验证码信息
 *
 * @author maurice
 */
public interface BuildToken extends Serializable, Expired {

    /**
     * 获取唯一识别
     *
     * @return 唯一识别
     */
    String getId();

    /**
     * 获取绑定 token 值
     *
     * @return token
     */
    String getToken();

    /**
     * 获取验证码类型
     *
     * @return 类型
     */
    String getType();

    /**
     * 获取提交时的绑定 token 参数名称
     *
     * @return 提交时的绑定 token 参数名称
     */
    String getParamName();

    /**
     * 获取过期时间
     *
     * @return 过期时间
     */
    Duration getExpireTime();

    /**
     * 设置过期时间
     *
     * @param expireTime 过期时间
     */
    void setExpireTime(Duration expireTime);

    /**
     * 设置拦截 token
     *
     * @param token 绑定 token
     */
    void setInterceptToken(BuildToken token);

    /**
     * 获取拦截 token
     *
     * @return 拦截 token
     */
    BuildToken getInterceptToken();

    /**
     * 获取构造验证码参数信息
     *
     * @return 构造验证码参数信息
     */
    Map<String, Object> getArgs();
}
