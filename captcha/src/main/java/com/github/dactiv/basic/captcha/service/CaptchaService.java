package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.framework.commons.RestResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 验证码服务
 *
 * @author maurice
 */
public interface CaptchaService {

    /**
     * 生成 token
     *
     * @param deviceIdentified 设备唯一是被
     * @return token
     */
    BuildToken generateToken(String deviceIdentified);

    /**
     * 验证请求
     *
     * @param request http servlet 请求
     *
     * @return 验证结果集
     */
    RestResult<Map<String, Object>> verify(HttpServletRequest request);

    /**
     * 验证 token
     *
     * @param buildToken     绑定 token
     * @param requestCaptcha 提交过来的验证码
     * @return 验证结果集
     */
    RestResult<Map<String, Object>> verify(BuildToken buildToken, String requestCaptcha);

    /**
     * 获取验证码类型
     *
     * @return 验证码类型
     */
    String getType();

    /**
     * 生成验证码
     *
     * @param request http servlet request
     * @return 验证码结果
     * @throws Exception 生成错误时抛出
     */
    Object generateCaptcha(HttpServletRequest request) throws Exception;

    /**
     * 是否支持本次请求
     *
     * @param request http servlet request
     * @return true 是，否则 false
     */
    boolean isSupport(HttpServletRequest request);

    /**
     * 获取绑定 token
     *
     * @param token token 值
     * @return 绑定 token
     */
    BuildToken getBuildToken(String token);

    /**
     * 获取创建验证码的参数信息
     *
     * @return 参数信息 map
     */
    Map<String, Object> getCreateArgs();

    /**
     * 获取提交时的绑定 token 参数名称
     *
     * @return 名称
     */
    String getTokenParamName();

    /**
     * 保存绑定 token
     *
     * @param token 绑定 token
     */
    void saveBuildToken(BuildToken token);

    /**
     * 获取提交时的验证码参数名称
     *
     * @return 参数名称
     */
    default String getCaptchaParamName() {
        return null;
    }

    /**
     * 获取使用验证码的账户名称
     *
     * @return 使用验证码的账户名称
     */
    default String getUsernameParamName() {
        return null;
    }
}
