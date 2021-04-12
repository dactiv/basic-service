
package com.fuyu.basic.support.crypto.algorithm.exception;

/**
 * 加密解密异常,当加密解密错误时候抛出。
 *
 * @author maurice
 */
public class CryptoException extends RuntimeException {

    private static final long serialVersionUID = -6698840153536223973L;

    /**
     * 加密解密异常
     */
    public CryptoException() {
    }

    /**
     * 加密解密异常
     *
     * @param message 异常信息
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * 加密解密异常
     *
     * @param message 异常信息
     * @param cause   异常类
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 加密解密异常
     *
     * @param cause 异常类
     */
    public CryptoException(Throwable cause) {
        super(cause);
    }

}
