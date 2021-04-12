
package com.fuyu.basic.support.crypto.algorithm.exception;

/**
 * 校验签名异常，在非对称解密校验错误时抛出
 *
 * @author maurice
 */
public class VerifyException extends CryptoException {

    private static final long serialVersionUID = 6695940887376029091L;

    /**
     * 校验代码
     */
    private String verifyCode;

    /**
     * 校验签名异常
     */
    public VerifyException() {
    }

    /**
     * 校验签名异常
     *
     * @param verifyCode 校验代码
     * @param message    异常信息
     */
    public VerifyException(String verifyCode, String message) {
        super(message);
        this.verifyCode = verifyCode;
    }

    /**
     * 校验签名异常
     *
     * @param verifyCode 校验代码
     * @param message    异常信息
     * @param cause      异常类
     */
    public VerifyException(String verifyCode, String message, Throwable cause) {
        super(message, cause);
        this.verifyCode = verifyCode;
    }

    /**
     * 校验签名异常
     *
     * @param verifyCode 校验代码
     * @param cause      异常类
     */
    public VerifyException(String verifyCode, Throwable cause) {
        super(cause);
        this.verifyCode = verifyCode;
    }

    /**
     * 获取校验代码
     *
     * @return 校验代码
     */
    public String getVerifyCode() {
        return verifyCode;
    }
}
