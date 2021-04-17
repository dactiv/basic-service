package com.github.dactiv.basic.config.service;


import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ServiceInfo;
import com.github.dactiv.framework.spring.web.endpoint.EnumerateEndpoint;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 枚举资源服务
 *
 * @author maurice.chen
 */
@Component
@SuppressWarnings("unchecked")
public class DiscoveryEnumerateResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEnumerateResourceService.class);

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_ENUMERATE_INFO_URL = "actuator/enumerate";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 当前数据
     */
    private final Map<String, Map<String, Map<String, Object>>> data = new LinkedHashMap<>();

    /**
     * 不需要同步的服务
     */
    private final List<String> notSyncServiceList = new ArrayList<>();

    /**
     * 同步插件资源，默认每三十秒扫描一次 discovery 的 服务信息
     */
    @Scheduled(cron = "${spring.application.enum.sync.cron.expression:0 0/10 * * * ?}")
    public void syncEnumerate() {
        // 获取所有服务
        discoveryClient
                .getServices()
                .stream()
                .map(this::getMaxVersionServiceInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addEnumerateResource);
    }

    /**
     * 清除不同步的服务
     */
    @Scheduled(cron = "${spring.application.enum.sync.cron.expression:0 0/30 * * * ?}")
    public void cleanNotSyncServiceList() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("开始清除不同步的服务[" + notSyncServiceList + "]");
        }
        notSyncServiceList.clear();
    }

    /**
     * 获取枚举信息
     *
     * @param service  服务名称
     * @param enumName 枚举名称
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
     * 添加枚举资源
     *
     * @param serviceInfo 服务信息
     */
    private void addEnumerateResource(ServiceInfo serviceInfo) {
        if (serviceInfo.getInfo() != null && serviceInfo.getInfo().containsKey(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME)) {

            Map<String, Map<String, Object>> data = Casts.cast(serviceInfo.getInfo().get(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME));

            if (MapUtils.isEmpty(data)) {
                notSyncServiceList.add(serviceInfo.getService());
            } else {

                this.data.put(serviceInfo.getService(), data);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("添加服务[" + serviceInfo.getService() + "]枚举资源[" + data + "]成功");
                }
            }

        } else if (!notSyncServiceList.contains(serviceInfo.getService())) {
            notSyncServiceList.add(serviceInfo.getService());
        }
    }

    /**
     * 获取最大版本的服务信息
     *
     * @param service 服务名称
     * @return 服务信息
     */
    private Optional<ServiceInfo> getMaxVersionServiceInfo(String service) {

        return discoveryClient
                .getInstances(service)
                .stream()
                .map(this::createServiceInfo)
                .filter(Objects::nonNull)
                .filter(c -> c.getVersion() != null)
                .max(Comparator.comparing(ServiceInfo::getVersion));
    }

    /**
     * 创建服务信息
     *
     * @param serviceInstance 服务实例
     * @return 服务信息
     */
    private ServiceInfo createServiceInfo(ServiceInstance serviceInstance) {
        if (!notSyncServiceList.contains(serviceInstance.getServiceId())) {
            Map<String, Object> info = getInstanceInfo(serviceInstance);

            if (info == null) {
                return null;
            }

            return ServiceInfo.build(serviceInstance.getServiceId(), info);
        } else {
            return null;
        }
    }

    /**
     * 获取实例 info
     *
     * @param instance 实例
     * @return 实例信息
     */
    private Map<String, Object> getInstanceInfo(ServiceInstance instance) {

        if (instance == null) {
            return new LinkedHashMap<>();
        }

        String ip = instance.getHost();
        int port = instance.getPort();

        try {
            return restTemplate.getForObject(getEnumerateInfoUrl(ip, port), Map.class);
        } catch (Exception e) {
            LOGGER.warn("通过 url [" + getEnumerateInfoUrl(ip, port) + "] 获取信息出现异常");
        }

        return null;
    }

    /**
     * 获取应用信息连接
     *
     * @param ip   ip地址
     * @param port 端口
     * @return 连接 url
     */
    public String getEnumerateInfoUrl(String ip, int port) {
        return "http://" + ip + ":" + port + "/" + DEFAULT_ENUMERATE_INFO_URL;
    }

    /**
     * 获取服务枚举名称
     *
     * @param service 服务名
     * @return 枚举名称集合
     */
    public Set<String> getServiceEnumerateName(String service) {
        return data.get(service).keySet();
    }
}
