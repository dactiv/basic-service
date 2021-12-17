package com.github.dactiv.basic.commons;

import com.github.dactiv.framework.commons.Casts;

/**
 * 系统常量
 *
 * @author maurice.chen
 */
public interface Constants {

    String WEB_FILTER_RESULT_ID = "web";

    String CHAT_FILTER_RESULT_ID = "chat";

    /**
     * 替换 HTML 标签正则表达式
     */
    String REPLACE_HTML_TAG_REX = "<[.[^<]]*>";

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
     * socket 服务系统名称
     */
    String SYS_SOCKET_SERVER_NAME = "socket-server";

    /**
     * 权限系统的默认 rabbitmq 交换机名称
     */
    String SYS_AUTHENTICATION_RABBITMQ_EXCHANGE = SYS_AUTHENTICATION_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * 消息系统的默认 rabbitmq 交换机名称
     */
    String SYS_MESSAGE_RABBITMQ_EXCHANGE = SYS_MESSAGE_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;

    /**
     * socket 服务的默认 rabbitmq 交换机名称
     */
    String SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE = SYS_SOCKET_SERVER_NAME + Casts.DEFAULT_DOT_SYMBOL + RABBITMQ_EXCHANGE;
}
