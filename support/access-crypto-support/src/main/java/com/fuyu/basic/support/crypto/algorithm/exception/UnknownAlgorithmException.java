
package com.fuyu.basic.support.crypto.algorithm.exception;

/**
 * 未知的加密解密算法异常，当存在不支出的加密解密算法时抛出
 *
 * @author maurice
 */
public class UnknownAlgorithmException extends CryptoException {

    private static final long serialVersionUID = -1361033500633398137L;

    /**
     * 未知的加密解密算法异常
     *
     * @param message 异常信息
     */
    public UnknownAlgorithmException(String message) {
        super(message);
    }

    /**
     * 未知的加密解密算法异常
     *
     * @param cause 异常类
     */
    public UnknownAlgorithmException(Throwable cause) {
        super(cause);
    }

    /**
     * 未知的加密解密算法异常
     *
     * @param message 异常信息
     * @param cause   异常类
     */
    public UnknownAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }
}
