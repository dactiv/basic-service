
package com.fuyu.basic.support.crypto.algorithm.exception;

/**
 * 编码异常类，在转换编码错误或找不到编码类型时抛出
 *
 * @author maurice
 */
public class CodecException extends RuntimeException {

    private static final long serialVersionUID = 2903493323137209720L;

    /**
     * 编码异常类
     */
    public CodecException() {
    }

    /**
     * 编码异常类
     *
     * @param message 异常信息
     */
    public CodecException(String message) {
        super(message);
    }

    /**
     * 编码异常类
     *
     * @param message 异常信息
     * @param cause   异常类
     */
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 编码异常类
     *
     * @param cause 异常类
     */
    public CodecException(Throwable cause) {
        super(cause);
    }
}
