package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 认证配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("spring.security.authentication")
public class AuthenticationProperties {

    /**
     * 允许认证错误次数，当达到峰值时，出现验证码
     */
    private Integer allowableFailureNumber = 1;

    /**
     * 用户名密码登录错误使用的验证码类型
     */
    private String usernameFailureCaptchaType = "picture";

    /**
     * 手机号码认证错误使用的验证码类型
     */
    private String mobileFailureCaptchaType = "picture";

    /**
     * 短信 token 参数名称，用于手机号码认证错误时，
     * 在次发送短信验证码时，需要 mobileFailureCaptchaType 类型的验证码通过才能发送短信验证码
     */
    private String smsCaptchaParamName = "_smsCaptchaToken";

    /**
     * 错误次数超时间
     */
    private TimeProperties allowableFailureNumberExpireTime = new TimeProperties(1800, TimeUnit.SECONDS) ;

    /**
     * 允许认证错误次数的 redis key 前缀
     */
    private String allowableFailureNumberKeyPrefix = "spring.security:authentication:failure:";

    /**
     * 注册配置
     */
    private Register register = new Register();

    /**
     * 移动端配置
     */
    private Mobile mobile = new Mobile();

    /**
     * 验证码配置
     */
    private Captcha captcha = new Captcha();

    /**
     * 异地区域配置
     */
    private AbnormalArea abnormalArea = new AbnormalArea();

    /**
     * 注册配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    public static class Register {

        /**
         * 随机登陆账户位数
         */
        private int randomUsernameCount = 6;

        /**
         * 你用户注册的默认分组
         */
        private int defaultGroup = 2;

        /**
         * 随机密码位数
         */
        private int randomPasswordCount = 16;
    }

    /**
     * 验证码配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    public static class Captcha {

        /**
         * 验证码验证名称
         */
        private String captchaParamName = "_smsCaptcha";

        /**
         * 对应验证码类型的 token 参数名称
         */
        private String tokenParamName = "_smsCaptchaToken";
    }

    /**
     * 移动端配置
     *
     * @author maurice.chen
     */
    @Data
    @NoArgsConstructor
    public static class Mobile {

        /**
         * 登陆账户的参数名
         */
        private String usernameParamName = "phoneNumber";

        /**
         * 移动端认证缓存 key 名称
         */

        private String cacheName = "spring:security:authentication:mobile:token:";
        /**
         * token 名称
         */
        private String paramName = "token";

        /**
         * 缓存超时时间
         */
        private TimeProperties expiresTime = new TimeProperties(2592, TimeUnit.SECONDS);

    }

    /**
     * 异地区域配置
     */
    @Data
    @NoArgsConstructor
    public static class AbnormalArea {

        /**
         * 发送站内信的消息内容
         */
        private String sendContent = "您的账户在异地登录，如果非本人操作。请及时修改密码。";

        /**
         * 发送站内信的标题内容
         */
        private String title = "异地登录通知";

        /**
         * 发送站内信的消息类型
         */
        private String messageType = "system";

        /**
         * 使用哪个用户发送
         */
        private Integer fromUserId = 1;

        /**
         * 发送失败后的重试次数
         */
        private Integer maxRetryCount = 3;
    }

}
