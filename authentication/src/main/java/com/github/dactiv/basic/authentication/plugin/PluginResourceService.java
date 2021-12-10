package com.github.dactiv.basic.authentication.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.basic.authentication.domain.model.ResourceModel;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.event.NacosInstancesChangeEvent;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceSubscribeEvent;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.plugin.PluginInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件資源管理
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class PluginResourceService {

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    private final RestTemplate restTemplate;

    private final NacosSpringEventManager nacosSpringEventManager;

    private final AuthorizationService authorizationService;

    private final List<PluginResourceInterceptor> pluginResourceInterceptor;

    /**
     * 服务实例缓存，用于记录当前的插件信息是否需要更新资源
     */
    private final Map<String, List<PluginInstance>> instanceCache = new LinkedHashMap<>();

    /**
     * 当前插件的所有资源集合
     */
    private final List<ResourceModel> resources = new LinkedList<>();

    public PluginResourceService(RestTemplate restTemplate,
                                 NacosSpringEventManager nacosSpringEventManager,
                                 AuthorizationService authorizationService,
                                 List<PluginResourceInterceptor> pluginResourceInterceptor) {
        this.restTemplate = restTemplate;
        this.nacosSpringEventManager = nacosSpringEventManager;
        this.authorizationService = authorizationService;
        this.pluginResourceInterceptor = pluginResourceInterceptor;
    }

    /**
     * 获取实例 info
     *
     * @param instance 实例
     *
     * @return 实例信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInstanceInfo(Instance instance) {

        String http = StringUtils.prependIfMissing(instance.toInetAddr(), "http://");
        String url = StringUtils.appendIfMissing(http, DEFAULT_PLUGIN_INFO_URL);

        return restTemplate.getForObject(url, Map.class);
    }

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

    @Concurrent(
            value = "sync:plugin:resource:[#groupName]:[#serviceName]",
            exception = "同步插件信息遇到并发，不执行重试操作",
            waitTime = @Time(0L)
    )
    public void syncPluginResource(String groupName, String serviceName, List<Instance> instances) {

        Optional<Instance> optional = instances.stream().max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [" + serviceName + "] 的最多版本实例");
            return;
        }

        Instance instance = optional.get();
        // 获取实例版本信息
        Version version = getInstanceVersion(instance);

        PluginInstance pluginInstance = Casts.of(instance, PluginInstance.class);
        pluginInstance.setServiceName(serviceName);
        pluginInstance.setVersion(version);

        List<PluginInstance> cache = instanceCache.computeIfAbsent(groupName, k -> new LinkedList<>());

        Optional<PluginInstance> exist = cache
                .stream()
                .filter(c -> c.getServiceName().equals(pluginInstance.getServiceName()))
                .findFirst();
        // 判断一下当前缓存是否存在同样的实例，如果存在，判断缓存的实例版本和当前的实例版本，如果当前实例版本较大，在覆盖一次資源内容
        if (exist.isPresent()) {

            PluginInstance existData = exist.get();

            if (existData.getVersion().compareTo(pluginInstance.getVersion()) > 0) {
                return;
            }

            cache.remove(existData);
        }

        Map<String, Object> info = getInstanceInfo(instance);
        pluginInstance.setInfo(info);

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
        String applicationName = instance.getServiceName();

        if (log.isDebugEnabled()) {
            log.debug("开始绑定[" + applicationName + "]资源信息，当前版本为:" + instance.getVersion());
        }

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getInfo());
        // 启用資源得到新的資源集合
        List<ResourceModel> newResourceList = pluginList
                .stream()
                .map(p -> createResource(p, instance,null))
                .collect(Collectors.toList());

        List<ResourceModel> unmergeResourceList = TreeUtils.unBuildGenericTree(newResourceList);

        resources.removeIf(r -> r.getApplicationName().equals(instance.getServiceName()));
        resources.addAll(unmergeResourceList);

        if (log.isDebugEnabled()) {
            log.debug("绑定 [" + applicationName + "] 资源信息完成");
        }

        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postSyncPlugin(instance, unmergeResourceList));
        }
    }

    /**
     * 通过 info 信息创建插件信息实体集合
     *
     * @param info info 信息
     *
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
     *
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
     * 创建資源
     *
     * @param plugin   插件信息
     * @param instance 服务信息
     * @param parent   夫类资源
     *
     * @return 新的資源
     */
    private ResourceModel createResource(PluginInfo plugin, PluginInstance instance, ResourceModel parent) {
        ResourceModel target = Casts.of(plugin, ResourceModel.class, PluginInfo.DEFAULT_CHILDREN_NAME);

        if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        } else if (Objects.nonNull(parent)){
            target.setParentId(parent.getId());
        }

        if (StringUtils.isBlank(target.getApplicationName())) {
            target.setApplicationName(instance.getServiceName());
        }

        if (instance.getVersion() != null) {
            target.setVersion(instance.getVersion().toString());
        }

        target.setCode(plugin.getId());
        target.setId(DigestUtils.md5DigestAsHex(String.valueOf(target.hashCode()).getBytes(StandardCharsets.UTF_8)));

        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .map(c -> createResource(Casts.cast(c, PluginInfo.class), instance, target))
                .forEach(r -> target.getChildren().add(r));

        return target;
    }

    /**
     * 禁用资源
     *
     * @param nacosService nacos 服务信息
     */
    public void disabledApplicationResource(NacosService nacosService) {

        List<ResourceSourceEnum> sources = resources
                .stream()
                .filter(r -> r.getApplicationName().equals(nacosService.getName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .collect(Collectors.toList());

        resources.removeIf(r -> r.getApplicationName().equals(nacosService.getName()));
        // 查询所有符合条件的资源,并设置为禁用状态
        authorizationService.deleteAuthorizationCache(sources);
        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postDisabledApplicationResource(nacosService));
        }
    }

    /**
     * 监听 nacos 服务被订阅事件，自动同步插件資源
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosServiceSubscribeEvent(NacosServiceSubscribeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());
        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 监听nasoc 服务变化事件
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosInstancesChangeEvent(NacosInstancesChangeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());

        if (CollectionUtils.isEmpty(nacosService.getInstances())) {
            disabledApplicationResource(nacosService);
            return;
        }

        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 重新订阅所有服务
     */
    @Concurrent(value = "subscribe_or_unsubscribe:plugin", exception = "正在执行，请稍后在试。。", waitTime = @Time(0L))
    public void resubscribeAllService() {
        nacosSpringEventManager.expiredAllListener();
        nacosSpringEventManager.scanThenUnsubscribeService();
        nacosSpringEventManager.scanThenSubscribeService();
    }

    /**
     * 获取资源集合
     *
     * @return 资源集合
     */
    public List<ResourceModel> getResources() {
        return this
                .resources
                .stream()
                .map(r -> Casts.of(r, ResourceModel.class, PluginInfo.DEFAULT_CHILDREN_NAME))
                .collect(Collectors.toList());
    }
}
