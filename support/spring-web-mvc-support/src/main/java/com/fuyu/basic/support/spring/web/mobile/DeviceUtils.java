package com.fuyu.basic.support.spring.web.mobile;

import org.springframework.web.context.request.RequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 设备工具类
 *
 * @author maurice
 */
public class DeviceUtils {

    /**
     * 当前设备的 HttpServletRequest 属性的名称。
     */
    public static final String CURRENT_DEVICE_ATTRIBUTE = "currentDevice";
    /**
     * 请求头的设备唯一识别
     */
    public static final String REQUEST_DEVICE_IDENTIFIED_HEADER_NAME = "X-DEVICE-IDENTIFIED";
    /***
     * 请求参数的设备唯一识别
     */
    public static final String REQUEST_DEVICE_IDENTIFIED_PARAM_NAME = "deviceIdentified";

    /**
     * 从 Web 请求中提取当前设备.
     *
     * @param request the servlet request
     * @return 当前设备, 如果尚未为请求解析任何设备，则为null
     */
    public static Device getCurrentDevice(HttpServletRequest request) {
        return (Device) request.getAttribute(CURRENT_DEVICE_ATTRIBUTE);
    }

    /**
     * 从 Web 请求中提取当前设备,如果当前设备尚未解析，则抛出运行时异常。
     *
     * @param request the servlet request
     * @return 当前设备
     */
    public static Device getRequiredCurrentDevice(HttpServletRequest request) {
        Device device = getCurrentDevice(request);
        if (device == null) {
            throw new IllegalStateException("此请求中未设置任何当前设备，您是否配置了 DeviceResolverRequestFilter ？");
        }
        return device;
    }

    /**
     * 从 spring mvc 请求属性映射中提取当前设备.
     *
     * @param attributes the request attributes
     * @return 当前设备, 如果尚未为请求解析任何设备，则为null
     */
    public static Device getCurrentDevice(RequestAttributes attributes) {
        return (Device) attributes.getAttribute(CURRENT_DEVICE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }
}
