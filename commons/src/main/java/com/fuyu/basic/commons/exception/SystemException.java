package com.fuyu.basic.commons.exception;

/**
 * 系统异常基类
 *
 * @author maurice
 */
public class SystemException extends RuntimeException {

    /**
     * 系统异常
     */
    public SystemException() {
        super();
    }

    /**
     * 系统异常
     *
     * @param message 异常信息
     */
    public SystemException(String message) {
        super(message);
    }

    /**
     * 系统异常
     *
     * @param message 异常信息
     * @param cause   异常类
     * @since 1.4
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 系统异常
     *
     * @param cause 异常类
     * @since 1.4
     */
    public SystemException(Throwable cause) {
        super(cause);
    }

}

