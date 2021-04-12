package com.fuyu.basic.support.security.concurrent;

import com.fuyu.basic.commons.exception.SystemException;

/**
 * 并发异常
 *
 * @author maurice
 */
public class ConcurrentException extends SystemException {

    public ConcurrentException() {
    }

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentException(Throwable cause) {
        super(cause);
    }
}
