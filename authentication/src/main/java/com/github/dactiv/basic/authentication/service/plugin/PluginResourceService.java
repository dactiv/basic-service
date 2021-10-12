package com.github.dactiv.basic.authentication.service.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.event.NacosInstancesChangeEvent;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceSubscribeEvent;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.plugin.PluginInfo;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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
public class PluginResourceService {

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NacosSpringEventManager nacosSpringEventManager;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ApplicationConfig applicationConfig;

    private final Map<String, List<PluginInstance>> instanceCache = new LinkedHashMap<>();

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

        // 获取一次旧的资源，在递归完最新的资源后，把所有未匹配旧的资源全部删除掉
        List<Resource> oldResourceList = authorizationService.findResources(
                Wrappers
                        .<Resource>lambdaQuery()
                        .eq(Resource::getApplicationName, applicationName)
                        .in(Resource::getSource, Resource.DEFAULT_ALL_SOURCE_VALUES)
        );

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getInfo());
        // 启用資源得到新的資源集合
        List<Resource> newResourceList = pluginList
                .stream()
                .flatMap(p -> createResourceStream(p, instance))
                .flatMap(this::enabledApplicationResource)
                .collect(Collectors.toList());

        // 通过新的資源集合将 id 去重，得到新的資源 id
        List<Integer> newResourceIds = newResourceList
                .stream()
                .map(Resource::getId)
                .distinct()
                .collect(Collectors.toList());

        // 将新資源和旧資源进行对比，把旧的資源删除。
        List<Integer> oldResourceIds = oldResourceList
                .stream()
                .map(Resource::getId)
                .filter(id -> !newResourceIds.contains(id))
                .collect(Collectors.toList());

        oldResourceIds.forEach(id -> authorizationService.deleteResource(id));

        if (log.isDebugEnabled()) {
            log.debug("绑定 [" + applicationName + "] 资源信息完成");
        }

        authorizationService.deleteAuthorizationCache();

        Group group = authorizationService.getGroup(applicationConfig.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }

        List<Resource> groupResources = authorizationService.getGroupResources(group.getId(), applicationName);
        // 获取当前组对该应用持有的的資源 id
        List<Integer> currentGroupResourceIds = groupResources
                .stream()
                .map(Resource::getId)
                .distinct()
                .collect(Collectors.toList());
        // 根据组来源去遍历所有新資源里匹配的记录，并去除当前组里存在的資源，得到新的组資源 id 集合
        List<Resource> newGroupResource = newResourceList
                .stream()
                .filter(r -> authorizationService.isAdminGroupSource(group, r.getSource()))
                .filter(r -> !currentGroupResourceIds.contains(r.getId()))
                .collect(Collectors.toList());

        // 增加新資源
        authorizationService.addGroupResource(group, newGroupResource);
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
     * 启用资源
     *
     * @param resource 资源信息
     *
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
     *
     * @return 目标资源
     */
    private Resource getTargetResource(Resource resource) {
        // 根据唯一条件查询目标资源
        Resource target = authorizationService.fineOneResource(resource.getUniqueWrapper());

        if (Objects.isNull(target)) {
            target = resource;
        } else {

            // 如果数据库存在记录，将resource数据替换一次数据获取出来的值
            BeanUtils.copyProperties(
                    resource,
                    target,
                    IdEntity.ID_FIELD_NAME,
                    NumberIdEntity.CREATION_TIME_FIELD_NAME,
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
     *
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
     *
     * @return 新的資源
     */
    private Resource createResource(String source, PluginInfo plugin, PluginInstance instance) {
        Resource target = Casts.of(plugin, Resource.class, PluginInfo.DEFAULT_CHILDREN_NAME);

        target.setSource(ResourceSource.All.toString().equals(source) ? ResourceSource.Console.toString() : source);

        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .flatMap(c -> createResourceStream(c, instance))
                .forEach(r -> target.getChildren().add(r));

        if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        }

        if (StringUtils.isEmpty(target.getApplicationName())) {
            target.setApplicationName(instance.getServiceName());
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
            disabledApplicationResource(nacosService.getName());
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

}
