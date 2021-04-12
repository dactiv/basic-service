package com.fuyu.basic.support.security.audit;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.security.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 控制器审计方法拦截器
 *
 * @author maurice
 */
public class ControllerAuditHandlerInterceptor extends HandlerInterceptorAdapter implements ApplicationEventPublisherAware {

    private static final String DEFAULT_SUCCESS_SUFFIX_NAME = "SUCCESS";

    private static final String DEFAULT_FAILURE_SUFFIX_NAME = "FAILURE";

    private static final String DEFAULT_EXCEPTION_KEY_NAME = "exception";

    private ApplicationEventPublisher applicationEventPublisher;

    private String successSuffixName = DEFAULT_SUCCESS_SUFFIX_NAME;

    private String failureSuffixName = DEFAULT_FAILURE_SUFFIX_NAME;

    private String exceptionKeyName = DEFAULT_EXCEPTION_KEY_NAME;

    public ControllerAuditHandlerInterceptor() {
    }

    public ControllerAuditHandlerInterceptor(String successSuffixName,
                                             String failureSuffixName,
                                             String exceptionKeyName) {

        this.successSuffixName = successSuffixName;
        this.failureSuffixName = failureSuffixName;
        this.exceptionKeyName = exceptionKeyName;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (!HandlerMethod.class.isAssignableFrom(handler.getClass())) {
            return;
        }

        HandlerMethod handlerMethod = Casts.cast(handler);

        Map<String, Object> data;

        String type;

        String principal;

        Auditable auditable = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Auditable.class);

        if (auditable != null) {

            principal = getPrincipal(auditable.principal(), request);

            type = auditable.type();

            data = getData(request, response, handler);

            if (ex == null) {
                type = type + ":" + successSuffixName;
            } else {
                type = type + ":" + failureSuffixName;
                data.put(exceptionKeyName, ex.getMessage());
            }

        } else {
            Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Plugin.class);
            // 如果控制器方法带有 plugin 注解并且 audit 为 true 是，记录审计内容
            if (plugin != null && plugin.audit()) {

                principal = getPrincipal(null, request);

                type = plugin.name();

                Plugin root = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);

                if (root != null) {
                    type = root.name() + ":" + type;
                }

                data = getData(request, response, handler);

                if (ex == null) {
                    type = type + ":" + successSuffixName;
                } else {
                    type = type + ":" + failureSuffixName;
                    data.put(exceptionKeyName, ex.getMessage());
                }

            } else {
                return;
            }
        }

        AuditEvent auditEvent = new AuditEvent(LocalDateTime.now().toInstant(ZoneOffset.UTC), principal, type, data);

        // 推送审计事件
        applicationEventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));

    }

    private Map<String, Object> getData(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, String[]> parameterMap = request.getParameterMap();

        if (!parameterMap.isEmpty()) {
            data.putAll(parameterMap);
        }

        return data;
    }

    private String getPrincipal(String key, HttpServletRequest request) {

        String principal = null;

        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext.getAuthentication() == null || !securityContext.getAuthentication().isAuthenticated()) {

            if (StringUtils.isNotEmpty(key)) {
                principal = request.getParameter(key);

                if (StringUtils.isEmpty(principal)) {
                    principal = request.getHeader(key);
                }
            }

            if (StringUtils.isEmpty(principal)) {
                principal = request.getRemoteAddr();
            }

        } else {
            Authentication authentication = securityContext.getAuthentication();

            principal = authentication.getPrincipal().toString();

            Object detail = authentication.getDetails();

            if (SecurityUserDetails.class.isAssignableFrom(detail.getClass())) {
                SecurityUserDetails securityUserDetails = Casts.cast(detail);
                principal = securityUserDetails.getUsername();
            }
        }

        return principal;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public String getSuccessSuffixName() {
        return successSuffixName;
    }

    public String getFailureSuffixName() {
        return failureSuffixName;
    }

    public String getExceptionKeyName() {
        return exceptionKeyName;
    }
}
