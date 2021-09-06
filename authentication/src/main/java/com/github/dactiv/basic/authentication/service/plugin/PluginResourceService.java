package com.github.dactiv.basic.authentication.service.plugin;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.concurrent.LockType;
import com.github.dactiv.framework.spring.security.concurrent.annotation.Concurrent;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.plugin.PluginInfo;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 插件資源管理
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class PluginResourceService implements DisposableBean {

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    /**
     * 默认是否插件字段名称
     */
    public static final String DEFAULT_PLUGIN_NAME = "plugin";

    /**
     * 默认版本号字段名称
     */
    public static final String DEFAULT_VERSION_NAME = "version";

    /**
     * 默认插件字段名称
     */
    public static final String DEFAULT_ARTIFACT_ID_NAME = "artifact-id";

    /**
     * 默认组字段名称
     */
    public static final String DEFAULT_GROUP_ID_NAME = "group-id";

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PluginProperties properties;

    private final Map<String, List<ServiceEventListener>> listenerCache = new LinkedHashMap<>();

    private final Map<String, List<PluginInstance>> instanceCache = new LinkedHashMap<>();

    public PluginProperties getProperties() {
        return properties;
    }

    /**
     * 取消订阅超时服务插件
     *
     * @throws NacosException 执行 nacos 操作出错时抛出
     */
    @Concurrent(value = "subscribe_or_unsubscribe:plugin", exceptionMessage = "取消订阅服务遇到并发，不执行重试操作", type = LockType.Lock)
    @NacosCronScheduled(cron = "${authentication.plugin.unsubscribe-cron:0 0/5 * * * ?}", name = "取消订阅超时服务插件")
    public void unsubscribeAllExpiredService() throws NacosException {

        NamingService namingService = nacosServiceManager.getNamingService(
                discoveryProperties.getNacosProperties()
        );

        List<ServiceEventListener> listeners = listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (ServiceEventListener sel : listeners) {

            if (!sel.isExpired()) {
                continue;
            }

            namingService.unsubscribe(sel.getService().getName(), sel.getService().getGroupName(), sel);

            List<ServiceEventListener> list = listenerCache.get(sel.getService().getGroupName());

            list.remove(sel);
        }

    }

    /**
     * 订阅所有服务插件
     *
     * @throws NacosException 执行 nacos 操作出错时抛出
     */
    @Concurrent(value = "subscribe_or_unsubscribe:plugin", exceptionMessage = "订阅服务遇到并发，不执行重试操作", type = LockType.Lock)
    @NacosCronScheduled(cron = "${authentication.plugin.subscribe-cron:0 0/3 * * * ?}", name = "订阅所有服务插件")
    public void subscribeAllService() throws NacosException {

        NamingService namingService = nacosServiceManager.getNamingService(
                discoveryProperties.getNacosProperties()
        );

        NamingMaintainService namingMaintainService = nacosServiceManager.getNamingMaintainService(
                discoveryProperties.getNacosProperties()
        );

        ListView<String> view = namingService.getServicesOfServer(1, Integer.MAX_VALUE);

        for (String s : view.getData()) {

            Service service = namingMaintainService.queryService(s);

            List<Instance> instanceList = namingService.getAllInstances(service.getName(), service.getGroupName());

            List<Instance> pluginList = instanceList
                    .stream()
                    .filter(i -> i.containsMetadata(DEFAULT_PLUGIN_NAME))
                    .collect(Collectors.toList());

            if (pluginList.isEmpty()) {
                continue;
            }

            if (!pluginList.stream().allMatch(i -> BooleanUtils.toBoolean(i.getMetadata().get(DEFAULT_PLUGIN_NAME)))) {
                if (log.isDebugEnabled()) {
                    log.debug("[" + s + "] 服务不是插件服务，不做订阅。");
                }
                continue;
            }

            List<ServiceEventListener> listeners = listenerCache.computeIfAbsent(
                    service.getGroupName(),
                    k -> new LinkedList<>()
            );

            Optional<ServiceEventListener> optional = listeners
                    .stream()
                    .filter(l -> l.getService().getName().equals(s))
                    .findFirst();

            if (optional.isPresent()) {

                optional.get().setCreationTime(new Date());

                if (log.isDebugEnabled()) {
                    log.debug("[" + s + "] 服务已订阅，更新创建时间。");
                }

                continue;
            }

            ServiceEventListener listener = ServiceEventListener.of(
                    properties.getExpirationTime(),
                    service,
                    this
            );

            listeners.add(listener);

            log.info("订阅组为 [" + service.getGroupName() + "] 的 [" + s + "] 服务");

            namingService.subscribe(service.getName(), service.getGroupName(), listener);
            List<Instance> instances = namingService.getAllInstances(service.getName(), service.getGroupName());
            syncPluginResource(service.getGroupName(), service.getName(), instances);
        }
    }

    /**
     * 获取实例 info
     *
     * @param instance 实例
     * @return 实例信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getInstanceInfo(Instance instance) {

        String http = StringUtils.prependIfMissing(instance.toInetAddr(), "http://");
        String url = StringUtils.appendIfMissing(http, DEFAULT_PLUGIN_INFO_URL);

        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.warn("通过 url [" + url + "] 获取服务 [" + instance.getServiceName() + "] 的插件信息出现异常", e);
        }

        return null;
    }

    private int comparingInstanceVersion(Instance target, Instance source) {
        return getInstanceVersion(target).compareTo(getInstanceVersion(source));
    }

    public Version getInstanceVersion(Instance instance) {

        String version = instance.getMetadata().get(DEFAULT_VERSION_NAME);
        String groupId = instance.getMetadata().get(DEFAULT_GROUP_ID_NAME);
        String artifactId = instance.getMetadata().get(DEFAULT_ARTIFACT_ID_NAME);

        return VersionUtil.parseVersion(version, groupId, artifactId);
    }

    @Concurrent(
            value = "sync:plugin:resource:[#groupName]:[#serviceName]",
            exceptionMessage = "同步插件信息遇到并发，不执行重试操作",
            type = LockType.Lock
    )
    public void syncPluginResource(String groupName, String serviceName, List<Instance> instances) {

        Optional<Instance> optional = instances.stream().max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [" + serviceName + "] 的最多版本实例");
            return;
        }

        Instance instance = optional.get();

        Version version = getInstanceVersion(instance);
        Map<String, Object> info = getInstanceInfo(instance);

        PluginInstance pluginInstance = Casts.of(instance, PluginInstance.class);

        pluginInstance.setVersion(version);
        pluginInstance.setInfo(info);

        List<PluginInstance> cache = instanceCache.computeIfAbsent(groupName, k -> new LinkedList<>());

        Optional<PluginInstance> exist = cache
                .stream()
                .filter(c -> c.getServiceName().equals(pluginInstance.getServiceName()))
                .findFirst();

        if (exist.isPresent()) {

            PluginInstance existData = exist.get();

            if (existData.getVersion().compareTo(pluginInstance.getVersion()) > 0) {
                return;
            }

            cache.remove(existData);
        }

        cache.add(pluginInstance);

        enabledApplicationResource(pluginInstance);

    }

    /**
     * 启用应用资源
     *
     * @param instance 插件实例
     */
    public void enabledApplicationResource(PluginInstance instance) {

        if (Objects.isNull(instance) || Objects.isNull(instance.getVersion())) {
            return;
        }

        // 应用名称
        String applicationName = instance.getMetadata().get(PluginResourceService.DEFAULT_ARTIFACT_ID_NAME);

        if (log.isDebugEnabled()) {
            log.debug("开始绑定[" + applicationName + "]资源信息，当前版本为:" + instance.getVersion());
        }

        // 获取一次旧的资源，在递归完最新的资源后，把所有未匹配旧的资源全部删除掉
        List<Resource> oldResourceList = authorizationService.findResources(
                Wrappers
                        .<Resource>lambdaQuery()
                        .eq(Resource::getApplicationName, applicationName)
                        .in(Resource::getSource, Resource.DEFAULT_ALL_SOURCE_VALUES)
        );

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getInfo());

        // 遍历新资源，更新 serviceInfo 相关的资源信息
        List<Resource> newResourceList = pluginList
                .stream()
                .flatMap(p -> createResourceStream(p, instance))
                .flatMap(this::enabledApplicationResource)
                .distinct()
                .collect(Collectors.toList());

        // 遍历旧资源，将没有的資源删除掉
        List<Integer> oldResourceIds = oldResourceList
                .stream()
                .filter(resource -> !newResourceList.contains(resource))
                .map(Resource::getId)
                .collect(Collectors.toList());

        oldResourceIds.forEach(id -> authorizationService.deleteResource(id));

        if (log.isDebugEnabled()) {
            log.debug("绑定 [" + applicationName + "] 资源信息完成");
        }

        authorizationService.deleteAuthorizationCache();

        Group group = authorizationService.getGroup(properties.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }

        List<Resource> groupResources = authorizationService.getGroupResources(group.getId());
        // 获取后台新資源 id
        List<Integer> newResourceIds = Arrays.stream(StringUtils.split(group.getSource(), SpringMvcUtils.COMMA_STRING))
                .map(StringUtils::trim)
                .flatMap(s -> newResourceList.stream().filter(r -> isControllerResource(r, s)))
                .map(Resource::getId)
                .distinct()
                .collect(Collectors.toList());
        // 删除旧資源的 id
        List<Integer> newGroupResourceIds = groupResources
                .stream()
                .map(Resource::getId)
                .filter(id -> !oldResourceIds.contains(id))
                .collect(Collectors.toList());
        // 添加心資源 的 id
        newGroupResourceIds.addAll(newResourceIds);
        // 保存组
        authorizationService.saveGroup(group, newGroupResourceIds);
    }

    private boolean isControllerResource(Resource r, String s) {
        return r.getSource().equals(s) || Resource.DEFAULT_CONTAIN_SOURCE_VALUES.contains(r.getSource());
    }

    /**
     * 通过 info 信息创建插件信息实体集合
     *
     * @param info info 信息
     * @return 插件信息实体集合
     */
    private List<PluginInfo> createPluginInfoListFromInfo(Map<String, Object> info) {

        List<PluginInfo> result = new LinkedList<>();

        List<Map<String, Object>> pluginMapList = Casts.cast(info.get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME));

        for (Map<String, Object> pluginMap : pluginMapList) {
            PluginInfo pluginInfo = createPluginInfo(pluginMap);
            result.add(pluginInfo);
        }

        return result;
    }

    /**
     * 通过插件 map 创建插件信息实体
     *
     * @param pluginMap 插件 map
     * @return 插件信息实体
     */
    private PluginInfo createPluginInfo(Map<String, Object> pluginMap) {

        List<Map<String, Object>> children = new LinkedList<>();

        if (pluginMap.containsKey(PluginInfo.DEFAULT_CHILDREN_NAME)) {
            children = Casts.cast(pluginMap.get(PluginInfo.DEFAULT_CHILDREN_NAME));
            pluginMap.remove(PluginInfo.DEFAULT_CHILDREN_NAME);
        }

        PluginInfo pluginInfo = Casts.convertValue(pluginMap, PluginInfo.class);

        List<Tree<String, PluginInfo>> childrenNode = new LinkedList<>();

        pluginInfo.setChildren(childrenNode);

        for (Map<String, Object> child : children) {
            PluginInfo childNode = createPluginInfo(child);
            childrenNode.add(childNode);
        }

        return pluginInfo;
    }

    /**
     * 启用资源
     *
     * @param resource 资源信息
     * @return 流集合
     */
    private Stream<Resource> enabledApplicationResource(Resource resource) {

        // 获取目标资源
        Resource target = getTargetResource(resource);
        // 保存
        authorizationService.saveResource(target);

        List<Resource> result = resource.getChildren()
                .stream()
                .map(c -> Casts.cast(c, Resource.class))
                .peek(c -> c.setParentId(target.getId()))
                .flatMap(this::enabledApplicationResource)
                .collect(Collectors.toList());

        result.add(target);

        return result.stream();
    }

    /**
     * 获取目标资源，如果 resource 在数据库理存在数据，将获取数据库的记录，并用 resource 数据替换一次
     *
     * @param resource 当前资源
     * @return 目标资源
     */
    private Resource getTargetResource(Resource resource) {
        // 根据唯一条件查询目标资源
        Resource target = authorizationService.fineOneResource(resource.getUniqueWrapper());

        if (target == null) {
            target = resource;
        } else {
            // TODO 这里写得有点啰嗦
            if (StringUtils.isEmpty(resource.getName())) {
                resource.setName(target.getName());
            }

            if (StringUtils.isEmpty(resource.getIcon())) {
                resource.setIcon(target.getIcon());
            }

            if (StringUtils.isEmpty(resource.getApplicationName())) {
                resource.setApplicationName(target.getApplicationName());
            }

            if (StringUtils.isEmpty(resource.getVersion())) {
                resource.setVersion(target.getVersion());
            }

            // 设置该資源为启用状态
            resource.setStatus(DisabledOrEnabled.Enabled.getValue());

            // 如果数据库存在记录，将resource数据替换一次数据获取出来的值
            BeanUtils.copyProperties(
                    resource,
                    target,
                    IdEntity.ID_FIELD_NAME,
                    PluginInfo.DEFAULT_CHILDREN_NAME,
                    PluginInfo.DEFAULT_SOURCE_NAME
            );
        }

        return target;
    }

    /**
     * 创建资源
     *
     * @param entry    插件信息
     * @param instance 插件实例
     * @return 流集合
     */
    private Stream<Resource> createResourceStream(Tree<String, PluginInfo> entry,
                                                  PluginInstance instance) {

        PluginInfo plugin = Casts.cast(entry);

        return Arrays.stream(StringUtils.split(plugin.getSource(), SpringMvcUtils.COMMA_STRING))
                .map(source -> createResource(source, plugin, instance));
    }

    /**
     * 创建資源
     *
     * @param source   資源来源
     * @param plugin   插件信息
     * @param instance 服务信息
     * @return 新的資源
     */
    private Resource createResource(String source, PluginInfo plugin, PluginInstance instance) {
        Resource target = new Resource();

        BeanUtils.copyProperties(plugin, target, PluginInfo.DEFAULT_CHILDREN_NAME);

        target.setSource(ResourceSource.All.toString().equals(source) ? ResourceSource.Console.toString() : source);

        //List<Tree<String, PluginInfo>> children = plugin.getChildren();
        //List<Map<String, Object>> children = Casts.castIfNotNull(entry.get(PluginInfo.DEFAULT_CHILDREN_NAME));
        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .flatMap(c -> createResourceStream(c, instance))
                .forEach(r -> target.getChildren().add(r));

        if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        }

        if (StringUtils.isEmpty(target.getApplicationName())) {
            target.setApplicationName(instance.getMetadata().get(PluginResourceService.DEFAULT_ARTIFACT_ID_NAME));
        }

        if (instance.getVersion() != null) {
            target.setVersion(instance.getVersion().toString());
        }

        target.setCode(plugin.getId());
        target.setStatus(DisabledOrEnabled.Enabled.getValue());
        target.setId(null);

        return target;
    }

    /**
     * 禁用资源
     *
     * @param applicationName 资源名称
     */
    public void disabledApplicationResource(String applicationName) {

        // 查询所有符合条件的资源,并设置为禁用状态
        List<Resource> resources = authorizationService.findResources(
                Wrappers
                        .<Resource>lambdaQuery()
                        .eq(Resource::getApplicationName, applicationName)
                        .in(Resource::getSource, Resource.DEFAULT_ALL_SOURCE_VALUES)
                        .eq(Resource::getStatus, DisabledOrEnabled.Enabled.getValue())
        );

        resources
                .stream()
                .peek(resource -> resource.setStatus(DisabledOrEnabled.Disabled.getValue()))
                .forEach(r -> authorizationService.saveResource(r));

        authorizationService.deleteAuthorizationCache();

    }

    /**
     * 监听本服务注册完成事件，当注册完成时候，同步所有插件菜单。
     *
     * @param o 事件原型
     * @throws NacosException 执行 nacos 操作出错时抛出
     */
    @EventListener
    public void onInstanceRegisteredEvent(InstanceRegisteredEvent<NacosAutoServiceRegistration> o) throws NacosException {
        subscribeAllService();
    }

    /**
     * 重新订阅所有服务
     */
    @Concurrent(value = "subscribe_or_unsubscribe:plugin", exceptionMessage = "取消订阅服务遇到并发，不执行重试操作", type = LockType.Lock)
    public void resubscribeAllService() throws NacosException {
        listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(v -> v.setExpirationTime(new TimeProperties(0, TimeUnit.MILLISECONDS)));

        unsubscribeAllExpiredService();
        subscribeAllService();
    }

    @Override
    public void destroy() throws Exception {
        log.info("解除所有服务监听");
        listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(v -> v.setExpirationTime(new TimeProperties(0, TimeUnit.MILLISECONDS)));

        unsubscribeAllExpiredService();
    }
}
