package com.github.dactiv.basic.config.enumerate;

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
public class EnumerateServiceValidator implements NacosServiceListenerValidator {

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
        List<Instance> pluginList = nacosService
                .getInstances()
                .stream()
                .filter(i -> i.containsMetadata(DEFAULT_PLUGIN_NAME))
                .collect(Collectors.toList());

        if (pluginList.isEmpty()) {
            return false;
        }

        return true;
    }
}
