package com.fuyu.basic.commons.exception;


import org.springframework.http.HttpStatus;

/**
 * 带错误代码的异常
 *
 * @author maurice
 */
public class ErrorCodeException extends SystemException {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 带错误代码的异常
     *
     * @param message   异常信息
     * @param errorCode 错误代码
     */
    public ErrorCodeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带错误代码的异常
     *
     * @param message   异常信息
     * @param cause     异常类
     * @param errorCode 错误代码
     * @since 1.4
     */
    public ErrorCodeException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 带错误代码的异常
     *
     * @param cause     异常类
     * @param errorCode 错误代码
     * @since 1.4
     */
    public ErrorCodeException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * 错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause   异常类
     * @param code    错误代码
     * @param message 错误信息
     */
    public static void throwErrorCodeException(Throwable cause, String code, String message) {
        throw new ErrorCodeException(message, cause, code);
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause 异常类
     * @param code  错误代码
     */
    public static void throwErrorCodeException(Throwable cause, String code) {
        throwErrorCodeException(cause, code, cause.getMessage());
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause 异常类
     */
    public static void throwErrorCodeException(Throwable cause) {

        String message = HttpStatus.INTERNAL_SERVER_ERROR.name();
        String executeCode = HttpStatus.INTERNAL_SERVER_ERROR.toString();

        if (cause instanceof ErrorCodeException || cause instanceof ServiceException) {
            message = cause.getMessage();
            if (cause instanceof ErrorCodeException) {
                executeCode = ((ErrorCodeException) cause).getErrorCode();
            }
        }


        throwErrorCodeException(cause, executeCode, message);
    }
}

