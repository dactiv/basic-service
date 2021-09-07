package com.github.dactiv.basic.authentication.service.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceListenerValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件服务校验
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class PluginServiceValidator implements NacosServiceListenerValidator {

    /**
     * 默认是否插件字段名称
     */
    public static final String DEFAULT_PLUGIN_NAME = "plugin";


    @Override
    public boolean isSupport(NacosService nacosService) {
        return true;
    }

    @Override
    public boolean subscribeValid(NacosService nacosService) {
        List<Instance> pluginList = nacosService.getInstances()
                .stream()
                .filter(i -> i.containsMetadata(DEFAULT_PLUGIN_NAME))
                .collect(Collectors.toList());

        if (pluginList.isEmpty()) {
            return false;
        }

        if (!pluginList.stream().allMatch(i -> BooleanUtils.toBoolean(i.getMetadata().get(DEFAULT_PLUGIN_NAME)))) {
            if (log.isDebugEnabled()) {
                log.debug("[" + nacosService.getName() + "] 服务不是插件服务，不做订阅。");
            }
            return false;
        }

        return true;
    }
}
