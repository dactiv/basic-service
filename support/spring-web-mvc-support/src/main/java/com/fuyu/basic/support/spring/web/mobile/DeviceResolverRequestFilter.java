package com.fuyu.basic.support.spring.web.mobile;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 设备解析请求 filter, 用于通过 HttpServletRequest 获取设备信息
 *
 * @author maurice
 */
public class DeviceResolverRequestFilter extends OncePerRequestFilter {

    /**
     * 设备解析器
     */
    private final DeviceResolver deviceResolver;

    /**
     * 设备解析请求 filter
     */
    public DeviceResolverRequestFilter() {
        this(new LiteDeviceResolver());
    }

    /**
     * 设备解析请求 filter
     *
     * @param deviceResolver 设备解析器
     */
    public DeviceResolverRequestFilter(DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Device device = deviceResolver.resolveDevice(request);
        request.setAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE, device);
        filterChain.doFilter(request, response);
    }
}
