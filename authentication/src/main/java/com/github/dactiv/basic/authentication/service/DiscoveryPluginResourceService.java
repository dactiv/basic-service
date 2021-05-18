package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.entity.Group;
import com.github.dactiv.basic.authentication.dao.entity.Resource;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ServiceInfo;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.concurrent.LockType;
import com.github.dactiv.framework.spring.security.concurrent.annotation.Concurrent;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步插件资源服务
 *
 * @author maurice.chen
 */
@Component
@RefreshScope
@SuppressWarnings("unchecked")
public class DiscoveryPluginResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryPluginResourceService.class);

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "actuator/plugin";

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PluginProperties properties;

    /**
     * 当前服务缓存
     */
    private List<ServiceInfo> currentServices = new ArrayList<>();

    /**
     * 出现异常的服务
     */
    private final List<String> exceptionServices = new ArrayList<>();

    @NacosCronScheduled(cron = "${spring.security.plugin.clean-cron:0 0 0/2 * * ?}", name = "清除异常服务")
    public void cleanExceptionServices() {
        exceptionServices.clear();
    }

    /**
     * 同步插件资源，默认每三十秒扫描一次 discovery 的 服务信息
     */
    @NacosCronScheduled(cron = "${spring.security.plugin.sync-cron:30 * * * * ?}", name = "同步服务插件")
    @Concurrent(value = "sync:plugin:resource", exceptionMessage = "同步插件信息遇到并发，不执行重试操作", type = LockType.Lock)
    public void syncPluginResource() {

        // 获取所有服务
        List<String> services = discoveryClient.getServices();

        List<ServiceInfo> syncService = services.stream()
                .filter(s -> !exceptionServices.contains(s))
                .map(this::getMaxVersionSyncPluginEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(this::isMaxVersion)
                .map(this::enabledApplicationResource)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        // 遍历完成后如果 discovery 服务列表里的信息比 缓存的记录少，表示有某些服务下架，
        // 应该将所有资源取消
        if (syncService.size() < currentServices.size()) {
            currentServices.stream()
                    .filter(s -> !syncService.contains(s))
                    .forEach(s -> disabledApplicationResource(s.getService()));
        }

        authorizationService
                .findResources(
                        Wrappers
                                .<Resource>lambdaQuery()
                                .notIn(Resource::getApplicationName, services)
                )
                .forEach(r -> authorizationService.deleteResource(r.getId()));

        // 如果默认存在 admin 组，自动管理最新资源
        if (Objects.nonNull(properties.getAdminGroupId())) {

            Group group = authorizationService.getGroup(properties.getAdminGroupId());

            if (group != null) {

                List<Resource> newResourceList = authorizationService.findResources(new QueryWrapper<>());

                List<Integer> resourceIds = Arrays.stream(StringUtils.split(group.getSource(), ","))
                        .map(StringUtils::trim)
                        .flatMap(s -> newResourceList.stream().filter(r -> isControllerResource(r, s)))
                        .map(Resource::getId)
                        .distinct()
                        .collect(Collectors.toList());

                authorizationService.saveGroup(group, resourceIds);

            }

        }

        currentServices = syncService;
    }

    private boolean isControllerResource(Resource r, String s) {
        return r.getSource().equals(s) || Resource.DEFAULT_CONTAIN_SOURCE_VALUES.contains(r.getSource());
    }

    /**
     * 获取缓存中的服务信息集合
     *
     * @return 服务信息集合
     */
    private List<ServiceInfo> getCacheServiceInfos() {

        Iterable<String> iterable = redissonClient.getKeys().getKeysByPattern(properties.getCache().getName() + "*");

        List<String> keys = new LinkedList<>();

        CollectionUtils.addAll(keys, iterable.iterator());

        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys
                .stream()
                .filter(k -> !this.getClass().getName().equals(k))
                .map(k -> redissonClient.<ServiceInfo>getBucket(k).get())
                .collect(Collectors.toList());

    }

    /**
     * 获取存储在 redis 的插件资源服务 key 名称
     *
     * @param service 服务名称
     *
     * @return 插件资源服务 key 名称
     */
    private String getPluginResourceServiceKey(String service) {
        return properties.getCache().getName() + service;
    }

    /**
     * 判断是否服务为最大版本
     *
     * @param entity 同步插件实体
     *
     * @return 是 true，否则 false
     */
    private boolean isMaxVersion(ServiceInfo entity) {

        if (entity.getVersion().isSnapshot()) {
            return true;
        }

        List<ServiceInfo> serviceInfos = getCacheServiceInfos();

        if (!serviceInfos.isEmpty()) {

            Optional<ServiceInfo> serviceInfo = serviceInfos
                    .stream()
                    .filter(s -> s.getService().equals(entity.getService()))
                    .findFirst();

            if (serviceInfo.isPresent() && serviceInfo.get().getVersion().compareTo(entity.getVersion()) > 0) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + entity.getService() + "]服务当前版本小于缓存本本，不做更新");
                }

                return true;
            }

        }

        return false;
    }

    /**
     * 禁用服务资源
     *
     * @param service 服务名称
     */
    private void disabledApplicationResource(String service) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("检查到[" + service + "]服务已下架，开始禁用此服务资源");
        }

        // 判断是否并发，如果并发直接响应处理中
        authorizationService.disabledApplicationResource(service);
        redissonClient.getBucket(getPluginResourceServiceKey(service)).deleteAsync();
    }

    /**
     * 启用服务名称
     *
     * @param entity 同步插件实体
     */
    private ServiceInfo enabledApplicationResource(ServiceInfo entity) {

        List<ServiceInfo> serviceInfos = getCacheServiceInfos();

        if (serviceInfos.stream().anyMatch(s -> s.getService().equals(entity.getService()))) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + entity.getService() + "]服务已同步插件资源，无需重复同步");
            }

            return entity;
        }

        Map<String, Object> info = entity.getInfo();

        if (info.containsKey(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME)) {
            List<Map<String, Object>> plugins = Casts.cast(info.get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME));

            if (CollectionUtils.isNotEmpty(plugins)) {

                authorizationService.enabledApplicationResource(entity);

                redissonClient.getBucket(getPluginResourceServiceKey(entity.getService())).setAsync(
                        entity,
                        properties.getCache().getExpiresTime().getValue(),
                        properties.getCache().getExpiresTime().getUnit()
                );

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("对[" + entity.getService() + "]服务同步插件资源完成");
                }

                return entity;
            }
        }

        return null;
    }

    /**
     * 获取最大版本的实例
     *
     * @param service 服务名称
     *
     * @return 最大版本实例
     */
    private Optional<ServiceInfo> getMaxVersionSyncPluginEntity(String service) {

        return discoveryClient.getInstances(service)
                .stream()
                .map(i -> createServiceInfo(i, service))
                .filter(Objects::nonNull)
                .filter(c -> c.getVersion() != null)
                .max(Comparator.comparing(ServiceInfo::getVersion));
    }

    /**
     * 创建同步插件实体
     *
     * @param instance 服务实例
     * @param service  服务名称
     *
     * @return 同步插件实体
     */
    private ServiceInfo createServiceInfo(ServiceInstance instance, String service) {

        Map<String, Object> info = getInstanceInfo(instance);

        if (info == null) {
            return null;
        }

        return ServiceInfo.build(service, info);
    }

    /**
     * 获取实例 info
     *
     * @param instance 实例
     *
     * @return 实例信息
     */
    private Map<String, Object> getInstanceInfo(ServiceInstance instance) {
        if (instance == null) {
            return new LinkedHashMap<>();
        }

        String ip = instance.getHost();
        int port = instance.getPort();

        try {
            return restTemplate.getForObject(getPluginInfoUrl(ip, port), Map.class);
        } catch (Exception e) {
            exceptionServices.add(instance.getServiceId());
            LOGGER.warn("通过url[" + getPluginInfoUrl(ip, port) + "]获取信息出现异常", e);
        }

        return null;
    }

    /**
     * 获取应用信息连接
     *
     * @param ip   ip地址
     * @param port 端口
     *
     * @return 连接 url
     */
    public String getPluginInfoUrl(String ip, int port) {
        return "http://" + ip + ":" + port + "/" + DEFAULT_PLUGIN_INFO_URL;
    }
}
