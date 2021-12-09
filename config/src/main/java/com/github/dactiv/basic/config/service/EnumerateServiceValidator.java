package com.github.dactiv.basic.config.service;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.MapUtils;
import com.github.dactiv.basic.config.service.EnumerateResourceService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceListenerValidator;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.web.endpoint.EnumerateEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 插件服务校验
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class EnumerateServiceValidator implements NacosServiceListenerValidator {

    private final EnumerateResourceService enumerateResourceService;

    private final List<String> exceptionServices = new LinkedList<>();

    public EnumerateServiceValidator(EnumerateResourceService enumerateResourceService) {
        this.enumerateResourceService = enumerateResourceService;
    }

    @Override
    public boolean isSupport(NacosService nacosService) {
        return true;
    }

    @Override
    public boolean subscribeValid(NacosService nacosService) {

        if (exceptionServices.contains(nacosService.getName())) {
            return false;
        }

        Optional<Instance> optional = nacosService
                .getInstances()
                .stream()
                .max(enumerateResourceService::comparingInstanceVersion);

        if (optional.isEmpty()) {
            return false;
        }

        try {

            Instance instance = optional.get();

            Map<String, Object> data = enumerateResourceService.getInstanceEnumerate(instance);

            if (MapUtils.isEmpty(data)) {
                return false;
            }

            if (!data.containsKey(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME)) {
                return false;
            }
        } catch (Exception e) {
            log.warn("获取服务 [" + nacosService.getName() + "] 的枚举内容失败");

            if (HttpStatusCodeException.class.isAssignableFrom(e.getClass())) {
                HttpStatusCodeException exception = Casts.cast(e);

                if (Retryable.DEFAULT_SUPPORT_HTTP_STATUS.contains(exception.getStatusCode().value())) {
                    return false;
                }
            }

            exceptionServices.add(nacosService.getName());
            return false;
        }

        return true;
    }

    /**
     * 清除异常服务内容
     */
    @NacosCronScheduled(cron = "${enumerate.clear.exception-services-cron:0 0/5 * * * ? }")
    public void clearExceptionServices() {
        exceptionServices.clear();
    }
}
