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

    String SYS_MESSAGE_NAME = "message";

    String SYS_AUTHENTICATION_NAME = "authentication";

    String SYS_CONFIG_NAME = "config";

    String SYS_CAPTCHA_NAME = "captcha";

    String SYS_FILE_MANAGER_NAME = "file-manager";
}
