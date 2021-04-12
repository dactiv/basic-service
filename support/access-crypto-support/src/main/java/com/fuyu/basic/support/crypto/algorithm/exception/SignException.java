
package com.fuyu.basic.support.crypto.algorithm.exception;

/**
 * 签名错误异常，在非对称加密后密文签名错误时抛出
 *
 * @author maurice
 */
public class SignException extends CryptoException {

    private static final long serialVersionUID = -3663930076173498754L;

    /**
     * 签名错误异常
     */
    public SignException() {
    }

    /**
     * 签名错误异常
     *
     * @param message 异常信息
     */
    public SignException(String message) {
        super(message);
    }

    /**
     * 签名错误异常
     *
     * @param message 异常信息
     * @param cause   异常类
     */
    public SignException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 签名错误异常
     *
     * @param cause 异常类
     */
    public SignException(Throwable cause) {
        super(cause);
    }
}
