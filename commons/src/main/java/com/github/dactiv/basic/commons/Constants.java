package com.github.dactiv.basic.commons;

/**
 * 系统常量
 *
 * @author maurice.chen
 */
public interface Constants {

    /**
     * 默认 rabbitmq 的延迟交换机名称
     */
    String RABBITMQ_DELAY_EXCHANGE = "default.delay.exchange";

    /**
     * 消息系统名称
     */
    String SYS_MESSAGE_NAME = "message";

    /**
     * 权限系统名称
     */
    String SYS_AUTHENTICATION_NAME = "authentication";

    /**
     * 配置系统名称
     */
    String SYS_CONFIG_NAME = "config";

    /**
     * 验证码系统名称
     */
    String SYS_CAPTCHA_NAME = "captcha";

    /**
     * 文件管理系统名称
     */
    String SYS_FILE_MANAGER_NAME = "file-manager";
}
