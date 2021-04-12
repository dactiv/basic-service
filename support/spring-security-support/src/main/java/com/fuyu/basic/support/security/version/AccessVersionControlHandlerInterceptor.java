package com.fuyu.basic.support.security.version;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.support.security.plugin.Plugin;
import com.fuyu.basic.support.spring.web.mobile.Device;
import com.fuyu.basic.support.spring.web.mobile.DevicePlatform;
import com.fuyu.basic.support.spring.web.mobile.DeviceResolver;
import com.fuyu.basic.support.spring.web.mobile.LiteDeviceResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * 访问版本控制拦截器
 *
 * @author maurice
 */
public class AccessVersionControlHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final String DEFAULT_VERSION_HEAD_NAME = "X-APP-VERSION";

    private final DeviceResolver deviceResolver = new LiteDeviceResolver();

    private final ObjectMapper objectMapper;

    public AccessVersionControlHandlerInterceptor() {
        this(new ObjectMapper());
    }

    public AccessVersionControlHandlerInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = Casts.cast(handler, HandlerMethod.class);

            Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);

            if (plugin != null) {

                VersionControl[] versionControls = plugin.versionControls();

                if (ArrayUtils.isNotEmpty(versionControls)) {
                    return checkVersion(versionControls, request, response);
                }

            }

            return true;
        }

        return super.preHandle(request, response, handler);
    }

    private boolean checkVersion(VersionControl[] versionControls, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Device device = deviceResolver.resolveDevice(request);

        DevicePlatform requestDevicePlatform = device.getDevicePlatform();

        Optional<VersionControl> versionControlOptional = Arrays
                .stream(versionControls)
                .filter(v -> v.device().equals(requestDevicePlatform))
                .findFirst();

        if (!versionControlOptional.isPresent()) {
            return true;
        }

        VersionControl versionControl = versionControlOptional.get();

        Version minVersion = VersionUtil.parseVersion(versionControl.minVersion(), null, null);

        Version requestVersion = VersionUtil.parseVersion(request.getHeader(DEFAULT_VERSION_HEAD_NAME), null, null);

        if (!requestVersion.equals(Version.unknownVersion()) && minVersion.compareTo(requestVersion) > 0) {

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            RestResult<Map<String, Object>> r = new RestResult<>(
                    versionControl.responseStrategy().getName(),
                    versionControl.responseStrategy().getValue(),
                    RestResult.ERROR_EXECUTE_CODE
            );

            response.getWriter().write(objectMapper.writeValueAsString(r));

            return false;
        }

        return true;
    }
}
