package com.fuyu.basic.commons.exception;

/**
 * 带有名称枚举找不到内容异常
 *
 * @author maurice
 */
public class NameEnumNotFoundException extends SystemException {

    /**
     * 带有名称枚举找不到内容异常
     */
    public NameEnumNotFoundException() {
        super();
    }

    /**
     * 带有名称枚举找不到内容异常
     *
     * @param message 异常信息
     */
    public NameEnumNotFoundException(String message) {
        super(message);
    }

    /**
     * 带有名称枚举找不到内容异常
     *
     * @param message 异常信息
     * @param cause   异常类
     * @since 1.4
     */
    public NameEnumNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带有名称枚举找不到内容异常
     *
     * @param cause 异常类
     * @since 1.4
     */
    public NameEnumNotFoundException(Throwable cause) {
        super(cause);
    }
}

