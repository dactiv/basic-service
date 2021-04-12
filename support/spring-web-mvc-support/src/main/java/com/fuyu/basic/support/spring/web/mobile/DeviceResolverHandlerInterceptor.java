package com.fuyu.basic.support.spring.web.mobile;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 设备解析请求拦截器, 用于通过 HttpServletRequest 获取设备信息
 *
 * @author maurice
 */
public class DeviceResolverHandlerInterceptor extends HandlerInterceptorAdapter {

    /**
     * 设备解析器
     */
    private final DeviceResolver deviceResolver;

    /**
     * 设备解析请求拦截器
     */
    public DeviceResolverHandlerInterceptor() {
        this(new LiteDeviceResolver());
    }

    /**
     * 设备解析请求拦截器
     *
     * @param deviceResolver 设备解析器
     */
    public DeviceResolverHandlerInterceptor(DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Device device = deviceResolver.resolveDevice(request);
        request.setAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE, device);
        return true;
    }
}
