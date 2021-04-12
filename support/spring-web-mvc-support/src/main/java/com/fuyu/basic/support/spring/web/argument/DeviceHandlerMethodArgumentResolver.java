package com.fuyu.basic.support.spring.web.argument;

import com.fuyu.basic.support.spring.web.mobile.Device;
import com.fuyu.basic.support.spring.web.mobile.DeviceUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 设备参数解析器
 *
 * @author maurice
 */
public class DeviceHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Device.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest request, WebDataBinderFactory binderFactory) throws Exception {
        return DeviceUtils.getCurrentDevice(request);
    }
}
