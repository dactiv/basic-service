package com.fuyu.basic.commons.exception;

/**
 * 业务逻辑异常实现
 *
 * @author maurice.chen
 **/
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -5031974444998025805L;

    /**
     * 业务逻辑异常实现
     */
    public ServiceException() {
    }

    /**
     * 业务逻辑异常实现
     *
     * @param message 异常信息
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 业务逻辑异常实现
     *
     * @param message 错误信息
     * @param cause   异常类
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 业务逻辑异常实现
     *
     * @param cause 异常类
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * 业务逻辑异常实现
     *
     * @param message            错误信息
     * @param cause              异常类
     * @param enableSuppression  是否启用或禁用抑制 true 是，否则 false
     * @param writableStackTrace 是否生成栈追踪信息 true 是，否则 false
     */
    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
