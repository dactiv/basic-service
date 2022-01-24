package com.github.dactiv.basic.authentication.config;

import com.github.dactiv.basic.authentication.enumerate.GenderEnum;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.authentication.app")
public class ApplicationConfig {

    public static final String DEFAULT_LOGOUT_URL = "/logout";

    public static final List<String> DEFAULT_CAPTCHA_AUTHENTICATION_TYPES = Arrays.asList(
            ResourceSourceEnum.MOBILE_SOURCE_VALUE,
            ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
            DefaultUserDetailsService.DEFAULT_TYPES
    );

    /**
     * 管理员组 id
     */
    private Integer adminGroupId = 1;

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
     * 超级管理登陆账户
     */
    private String adminUsername = "admin";

    /**
     * 错误次数超时间
     */
    private TimeProperties allowableFailureNumberExpireTime = new TimeProperties(1800, TimeUnit.SECONDS);

    /**
     * 允许认证错误次数的 redis key 前缀
     */
    private String allowableFailureNumberKeyPrefix = "dactiv:authentication:failure:";

    /**
     * 登出连接
     */
    private String logoutUrl = DEFAULT_LOGOUT_URL;

    /**
     * 不需要验证码的认证类型
     */
    private List<String> captchaAuthenticationTypes = DEFAULT_CAPTCHA_AUTHENTICATION_TYPES;

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
     * 用户头像配置
     */
    private UserAvatar userAvatar = new UserAvatar();

    /**
     * id 解析器类型
     */
    private String ipResolverType = "ip.ws.126.net";

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
         * token 名称
         */
        private String paramName = "token";

        /**
         * 缓存配置
         */
        private CacheProperties cache = new CacheProperties(
                "spring:security:authentication:mobile:token:",
                new TimeProperties(7, TimeUnit.DAYS)
        );

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
         * 消息类型
         */
        private String messageType = "System";

        /**
         * 发送类型, siteMessage 为站内信
         */
        private String sendType = "site";

        /**
         * 发送失败后的重试次数
         */
        private Integer maxRetryCount = 3;
    }

    /**
     * 用户头像配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class UserAvatar {


        private final ResourceLoader resourceLoader = new DefaultResourceLoader();

        /**
         * 桶名称
         */
        private String bucketName = "authentication.user.avatar";

        /**
         * 历史文件 token
         */
        private String historyFileToken = "user_avatar_history_{0}.json";

        /**
         * 当前使用的头像名称
         */
        private String currentUseFileToken = "current_{0}";

        /**
         * 保留的历史头像记录总数
         */
        private Integer historyCount = 5;

        /**
         * 默认头像位置
         */
        private String defaultPath = "classpath:/avatar/";

        /**
         * 用户来源，用于默认服务启动时创建桶使用。
         */
        private List<String> userSources = Arrays.asList(
                ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                ResourceSourceEnum.USER_CENTER_SOURCE_VALUE,
                ResourceSourceEnum.SOCKET_USER_SOURCE_VALUE
        );

        /**
         * 获取默认头像路径
         *
         * @param genderEnum 性别
         * @param userId     用户主键 id
         *
         * @return 头像路径
         */
        public InputStream getDefaultAvatarPath(GenderEnum genderEnum, Integer userId) throws IOException {
            String folder = defaultPath + genderEnum.toString().toLowerCase();
            String[] fileList = Objects.requireNonNull(resourceLoader.getResource(folder).getFile().list());
            int index = userId % fileList.length;
            return resourceLoader.getResource(folder + File.separator + fileList[index]).getInputStream();
        }
    }

}
