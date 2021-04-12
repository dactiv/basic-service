package com.fuyu.basic.commons.spring.web;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * rest 结果集实体类，用于统一返回指定格式数据使用
 *
 * @author maurice.chen
 **/
public class RestResult<T> implements Serializable {

    private static final long serialVersionUID = 8809220528389402403L;

    public static final String SUCCESS_EXECUTE_CODE = String.valueOf(HttpStatus.OK.value());

    public static final String ERROR_EXECUTE_CODE = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());

    public static final String CAPTCHA_EXECUTE_CODE = "1001";

    public static final String DEFAULT_MESSAGE_NAME = "message";

    public static final String DEFAULT_DATA_NAME = "data";

    public static final String DEFAULT_STATUS_NAME = "status";

    public static final String DEFAULT_EXECUTE_CODE_NAME = "executeCode";

    public static final String DEFAULT_TIMESTAMP_NAME = "timestamp";

    /**
     * 信息
     */
    private String message;

    /**
     * 执行代码
     */
    private int status;

    /**
     * 执行代码
     */
    private String executeCode;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建时间
     */
    private Date timestamp = new Date();

    public RestResult() {
    }

    /**
     * 抽象的 rest 结果集实体类
     *
     * @param message 响应信息
     */
    public RestResult(String message) {
        this(message, HttpStatus.OK.value());
    }

    /**
     * 抽象的 rest 结果集实体类
     *
     * @param message 响应信息
     * @param status  执行状态
     */
    public RestResult(String message, int status) {
        this(message, status, SUCCESS_EXECUTE_CODE);
    }

    /**
     * 抽象的 rest 结果集实体类
     *
     * @param message     响应信息
     * @param status      执行状态
     * @param executeCode 执行代码
     */
    public RestResult(String message, int status, String executeCode) {
        this(message, status, executeCode, null);
    }

    /**
     * 抽象的 rest 结果集实体类
     *
     * @param message     响应信息
     * @param status      执行状态
     * @param executeCode 执行代码
     * @param data        响应数据
     */
    public RestResult(String message, int status, String executeCode, T data) {
        this.message = message;
        this.status = status;
        this.executeCode = executeCode;
        this.data = data;
    }

    /**
     * rest 结果集实体类
     *
     * @param executeCode 执行代码
     * @param e           异常信息
     */
    public RestResult(String executeCode, Throwable e) {
        this.executeCode = executeCode;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = e.getMessage();
    }

    /**
     * 获取信息
     *
     * @return 信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置信息
     *
     * @param message 信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取状态
     *
     * @return 状态
     * @see HttpStatus
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     * @see HttpStatus
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取执行代码
     *
     * @return 执行嗲吗
     */
    public String getExecuteCode() {
        return executeCode;
    }

    /**
     * 设置执行代码
     *
     * @param executeCode 执行代码
     */
    public void setExecuteCode(String executeCode) {
        this.executeCode = executeCode;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 设置创建时间
     *
     * @param timestamp 创建时间
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 消息结果集
     *
     * @param <T> 数据类型
     */
    public static class Result<T> {
        /**
         * 响应数据
         */
        private T data;

        /**
         * 响应信息
         */
        private String massage;

        public Result() {
        }

        /**
         * 消息结果集
         *
         * @param massage 响应消息
         * @param data    响应数据
         */
        public Result(String massage, T data) {
            this.data = data;
            this.massage = massage;
        }

        /**
         * 获取响应数据
         *
         * @return 响应数据
         */
        public T getData() {
            return data;
        }

        /**
         * 设置响应数据
         *
         * @param data 响应数据
         */
        public void setData(T data) {
            this.data = data;
        }

        /**
         * 获取响应消息
         *
         * @return 响应消息
         */
        public String getMassage() {
            return massage;
        }

        /**
         * 设置响应消息
         *
         * @param massage 响应消息
         */
        public void setMassage(String massage) {
            this.massage = massage;
        }
    }

    /**
     * 创建消息结果集
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     响应数据类型
     * @return 消息结果集
     */
    public static <T> Result<T> build(String message, T data) {
        return new Result<>(message, data);
    }

    /**
     * 创建消息结果集
     *
     * @param message 响应消息
     * @return 消息结果集
     */
    public static Result<Map<String, Object>> build(String message) {
        return new Result<>(message, new LinkedHashMap<>());
    }
}
