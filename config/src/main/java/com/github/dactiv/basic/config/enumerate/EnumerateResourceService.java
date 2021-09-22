package com.github.dactiv.basic.config.enumerate;


import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.MapUtils;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.nacos.event.NacosInstancesChangeEvent;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceSubscribeEvent;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.spring.security.plugin.PluginInfo;
import com.github.dactiv.framework.spring.web.endpoint.EnumerateEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 枚举资源服务
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class EnumerateResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumerateResourceService.class);

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_ENUMERATE_INFO_URL = "/actuator/enumerate";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NacosSpringEventManager nacosSpringEventManager;

    private final Map<String, Instance> instanceCache = new LinkedHashMap<>();

    /**
     * 当前数据
     */
    private final Map<String, Map<String, Map<String, Object>>> data = new LinkedHashMap<>();

    /**
     * 匹配最大版本实例
     *
     * @param target 目标实例
     * @param source 原实例
     *
     * @return 0 相等，小于0 小于，大于0 大于
     */
    public int comparingInstanceVersion(Instance target, Instance source) {
        return getInstanceVersion(target).compareTo(getInstanceVersion(source));
    }

    /**
     * 获取实例的版本信息
     *
     * @param instance 实例
     *
     * @return 版本信息
     */
    public Version getInstanceVersion(Instance instance) {

        String version = instance.getMetadata().get(PluginInfo.DEFAULT_VERSION_NAME);
        String groupId = instance.getMetadata().get(PluginInfo.DEFAULT_GROUP_ID_NAME);
        String artifactId = instance.getMetadata().get(PluginInfo.DEFAULT_ARTIFACT_ID_NAME);

        return VersionUtil.parseVersion(version, groupId, artifactId);
    }

    /**
     * 监听 nacos 服务被订阅事件，自动同步枚举数据
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosServiceSubscribeEvent(NacosServiceSubscribeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());
        syncEnumerateResource(nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 监听 nasoc 服务变化事件，自动同步枚举数据
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosInstancesChangeEvent(NacosInstancesChangeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());

        if (CollectionUtils.isEmpty(nacosService.getInstances())) {
            data.get(nacosService.getName()).clear();
            return;
        }

        syncEnumerateResource(nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 同步枚举資源
     *
     * @param serviceName 服务名称
     * @param instances   实例集合
     */
    private void syncEnumerateResource(String serviceName, List<Instance> instances) {
        Optional<Instance> optional = instances.stream().max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [" + serviceName + "] 的最多版本实例");
            return;
        }

        Instance instance = optional.get();

        Version version = getInstanceVersion(instance);

        if (instanceCache.containsKey(serviceName)) {
            Instance exist = instanceCache.get(serviceName);

            Version existVersion = getInstanceVersion(exist);

            if (existVersion.compareTo(version) > 0) {
                return;
            }
        }

        Map<String, Object> info = getInstanceEnumerate(instance);

        if (MapUtils.isEmpty(info)) {
            return;
        }

        if (!info.containsKey(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME)) {
            return;
        }

        Map<String, Map<String, Object>> enumerateData = Casts.cast(info.get(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME));

        this.data.put(serviceName, enumerateData);

        instanceCache.put(serviceName, instance);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("添加服务[" + serviceName + "]枚举资源[" + data + "]成功");
        }
    }

    /**
     * 获取枚举信息
     *
     * @param service  服务名称
     * @param enumName 枚举名称
     *
     * @return 枚举信息
     */
    public Map<String, Object> getServiceEnumerate(String service, String enumName) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (!this.data.containsKey(service)) {
            return result;
        }

        Map<String, Map<String, Object>> enumMap = this.data.get(service);

        if (!enumMap.containsKey(enumName)) {
            return result;
        }

        return enumMap.get(enumName);
    }

    /**
     * 获取实例枚举信息
     *
     * @param instance 实例
     *
     * @return 实例信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInstanceEnumerate(Instance instance) {

        String http = StringUtils.prependIfMissing(instance.toInetAddr(), "http://");
        String url = StringUtils.appendIfMissing(http, DEFAULT_ENUMERATE_INFO_URL);

        return restTemplate.getForObject(url, Map.class);
    }

    /**
     * 获取服务枚举
     *
     * @return 枚举名称集合
     */
    public Map<String, Map<String, Map<String, Object>>> getServiceEnumerate() {
        return data;
    }


    /**
     * 同步所有枚举資源
     */
    public void syncEnumerate() {
        nacosSpringEventManager.expiredAllListener();
        nacosSpringEventManager.scanThenUnsubscribeService();
        nacosSpringEventManager.scanThenSubscribeService();
    }
}
