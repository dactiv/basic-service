package com.github.dactiv.basic.commons;

import com.github.dactiv.framework.commons.Casts;

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
     * 默认 rabbitmq 交换机名称
     */
    String RABBITMQ_EXCHANGE = "default.exchange";

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

    /**
     * socket 结果集 id，用于响应socket 内容时的过滤或引入字段 id
     */
    String SOCKET_RESULT_ID = "socket_result";

    /**
     * 权限系统的默认 rabbitmq 交换机名称
     */
    String SYS_AUTHENTICATION_RABBITMQ_EXCHANGE = SYS_AUTHENTICATION_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 消息系统的默认 rabbitmq 交换机名称
     */
    String SYS_MESSAGE_RABBITMQ_EXCHANGE = SYS_MESSAGE_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;
}
