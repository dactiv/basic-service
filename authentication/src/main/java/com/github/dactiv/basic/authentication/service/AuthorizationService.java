package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.basic.authentication.dao.GroupDao;
import com.github.dactiv.basic.authentication.dao.ResourceDao;
import com.github.dactiv.basic.authentication.dao.entity.Group;
import com.github.dactiv.basic.authentication.dao.entity.Resource;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.IdEntity;
import com.github.dactiv.framework.commons.ServiceInfo;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.PrincipalAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.plugin.PluginInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 授权管理服务
 *
 * @author maurice.chen
 **/
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private PrincipalAuthenticationProvider authenticationProvider;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 超级管理员组 id
     */
    @Value("${spring.security.admin.group-id:1}")
    private Integer adminGroupId;


    /**
     * 获取账户认证的用户明细服务
     *
     * @param source 资源累袁
     * @return 账户认证的用户明细服务
     */
    public UserDetailsService getUserDetailsService(ResourceSource source) {

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                null,
                null,
                source.toString()
        );

        Optional<UserDetailsService> userDetailsService = authenticationProvider.getUserDetailsService(token);

        if (!userDetailsService.isPresent()) {
            throw new ServiceException("找不到类型为 [" + source + "] 的 UserDetailsService 实现");
        }

        return userDetailsService.get();
    }

    /**
     * 获取所有账户认证的用户明细服务
     *
     * @return 用户明细服务集合
     */
    public List<UserDetailsService> getUserDetailsServices() {
        return authenticationProvider.getUserDetailsServices();
    }

    // -------------------------------- 组管理 -------------------------------- //

    /**
     * 保存用户组
     *
     * @param group 用户组实体
     */
    public void saveGroup(Group group) {
        if (Objects.isNull(group.getId())) {
            insertGroup(group);
        } else {
            updateGroup(group);
        }
    }

    /**
     * 保存用户组
     *
     * @param group       用户组实体
     * @param resourceIds 资源 ID 集合
     */
    public void saveGroup(Group group, List<Integer> resourceIds) {

        List<ResourceSource> resourceSourceStream = Arrays.asList(ResourceSource.values());

        if (Arrays.stream(StringUtils.split(group.getSource(), ","))
                .anyMatch(s -> !resourceSourceStream.contains(ResourceSource.valueOf(s)))) {
            throw new ServiceException("组来源[" + group.getSource() + "]不在 " + resourceSourceStream + " 范围内");
        }

        saveGroup(group);

        if (resourceIds != null) {

            boolean isNotAnyMatchResourceSource = resourceIds
                    .stream()
                    .map(this::getResource)
                    .anyMatch(r -> Arrays.stream(StringUtils.split(group.getSource(), ","))
                            .map(StringUtils::trim)
                            .allMatch(s -> !s.equals(r.getSource())
                                    && !Resource.DEFAULT_CONTAIN_SOURCE_VALUES.contains(r.getSource())));

            if (isNotAnyMatchResourceSource) {
                throw new ServiceException("当前存在关联着不属于[" + group.getSourceName() + "]的资源");
            }

            groupDao.deleteResourceAssociation(group.getId());

            if (!resourceIds.isEmpty()) {
                groupDao.insertResourceAssociation(group.getId(), resourceIds);
            }
        }

    }

    /**
     * 新增用户组
     *
     * @param group 用户组实体
     */
    public void insertGroup(Group group) {
        Group entity = getGroupByFilter(group.getUniqueFilter());

        if (entity != null) {
            throw new ServiceException("用户组【" + group.getAuthority() + "】角色已存在");
        }

        groupDao.insert(group);
    }

    /**
     * 更新用户组
     *
     * @param group 用户组实体
     */
    public void updateGroup(Group group) {

        if (YesOrNo.No.getValue().equals(group.getCanBeModify())) {
            throw new ServiceException("用户组【" + group.getName() + "】不可修改");
        }

        groupDao.update(group);

        deleteAuthorizationCache();

    }

    /**
     * 删除用户组
     *
     * @param ids 主键 id 集合
     */
    public void deleteGroup(List<Integer> ids) {
        ids.forEach(this::deleteGroup);
    }

    /**
     * 删除用户组
     *
     * @param id 主键 id
     */
    public void deleteGroup(Integer id) {
        Group group = getGroup(id);

        if (group == null) {
            throw new ServiceException("找不到 ID 为 [" + id + "]的用户组记录");
        }

        if (group.getId().equals(adminGroupId)) {
            throw new ServiceException("不能删除管理员组");
        }

        if (YesOrNo.No.getValue().equals(group.getCanBeDelete())) {
            throw new ServiceException("用户组【" + group.getName() + "】不可删除");
        }

        Map<String, Object> filter = new HashMap<>(16);
        filter.put("parentIdEq", id);

        List<Group> groups = findGroups(filter);

        groups.forEach(g -> deleteGroup(g.getId()));

        groupDao.deleteResourceAssociation(id);
        groupDao.deleteConsoleUserAssociation(id);
        groupDao.deleteMemberUserAssociation(id);
        groupDao.delete(id);

        if (groups.isEmpty()) {
            deleteAuthorizationCache();
        }
    }

    /**
     * 获取用户组
     *
     * @param id 主键 id
     * @return 用户组实体
     */
    public Group getGroup(Integer id) {
        return getGroup(id, false);
    }

    /**
     * 获取用户组
     *
     * @param id   主键 id
     * @param lock 是否加锁
     * @return 用户组实体
     */
    public Group getGroup(Integer id, Boolean lock) {
        if (lock) {
            return groupDao.lock(id);
        }
        return groupDao.get(id);
    }

    /**
     * 获取系统用户所关联用户组
     *
     * @param userId 系统用户主键 id
     * @return 用户组集合
     */
    public List<Group> getConsoleUserGroups(Integer userId) {
        return groupDao.getConsoleUserGroups(userId);
    }

    /**
     * 获取会员用户所关联用户组
     *
     * @param userId 会员用户主键 id
     * @return 用户组集合
     */
    public List<Group> getMemberUserGroups(Integer userId) {
        return groupDao.getMemberUserGroups(userId);
    }

    /**
     * 获取用户组
     *
     * @param filter 查询条件
     * @return 用户组
     */
    public Group getGroupByFilter(Map<String, Object> filter) {
        List<Group> result = findGroups(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<Group> iterator = result.iterator();

        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 根据过滤条件查找用户组
     *
     * @param filter 过滤条件
     * @return 用户组实体集合
     */
    public List<Group> findGroups(Map<String, Object> filter) {
        return groupDao.find(filter);
    }

    // -------------------------------- 资源管理 -------------------------------- //

    /**
     * 保存资源
     *
     * @param resource 资源实体
     */
    public void saveResource(Resource resource) {
        if (Objects.isNull(resource.getId())) {
            insertResource(resource);
        } else {
            updateResource(resource);
        }
    }

    /**
     * 新增资源
     *
     * @param resource 资源实体
     */
    public void insertResource(Resource resource) {

        if (getResourceByFilter(resource.getUniqueFilter()) != null) {
            String msg = "资源[" + resource.getApplicationName() + "_" + resource.getCode() + "_" + resource.getType() + "]已存在";
            throw new ServiceException(msg);
        }

        resourceDao.insert(resource);
    }

    /**
     * 更新资源
     *
     * @param resource 资源实体
     */
    public void updateResource(Resource resource) {
        resourceDao.update(resource);

        deleteAuthorizationCache();

    }

    /**
     * 删除资源
     *
     * @param ids 主键 id 集合
     */
    public void deleteResources(List<Integer> ids) {
        ids.forEach(this::deleteResource);
    }

    /**
     * 删除资源
     *
     * @param id 主键 id
     */
    public void deleteResource(Integer id) {
        Map<String, Object> filter = new HashMap<>(16);
        filter.put("parentIdEq", id);

        List<Resource> resources = findResources(filter);

        resources.forEach(r -> deleteResource(r.getId()));

        resourceDao.deleteConsoleUserAssociation(id);
        resourceDao.deleteGroupAssociation(id);
        resourceDao.delete(id);

        if (resources.isEmpty()) {
            deleteAuthorizationCache();
        }
    }

    /**
     * 获取资源
     *
     * @param id 主键 id
     * @return 资源实体
     */
    public Resource getResource(Integer id) {
        return getResource(id, false);
    }

    /**
     * 获取资源
     *
     * @param id   主键 id
     * @param lock 是否加锁
     * @return 资源实体
     */
    public Resource getResource(Integer id, Boolean lock) {
        if (lock) {
            return resourceDao.lock(id);
        }
        return resourceDao.get(id);
    }

    /**
     * 获取资源
     *
     * @param filter 过滤条件
     * @return 资源实体
     */
    public Resource getResourceByFilter(Map<String, Object> filter) {
        List<Resource> result = findResources(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<Resource> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 获取资源
     *
     * @param groupId 组主键值
     * @return 资源实体集合
     */
    public List<Resource> getGroupResources(Integer groupId) {
        return resourceDao.getGroupResources(groupId);
    }

    /**
     * 获取系统用户关联资源
     *
     * @param userId 用户主键值
     * @return 资源实体集合
     */
    public List<Resource> getConsoleUserResources(Integer userId) {
        return resourceDao.getConsoleUserResources(userId);
    }

    /**
     * 获取系统用户关联(包含系统用户组关联，所有资源)资源实体集合
     *
     * @param userId 用户主键值
     * @return 资源实体集合
     */
    public List<Resource> getConsolePrincipalResources(Integer userId, Map<String, Object> filter) {
        return resourceDao.getConsolePrincipalResources(userId, filter);
    }

    /**
     * 根据过滤条件查找资源
     *
     * @param filter 过滤条件
     * @return 资源实体集合
     */
    public List<Resource> findResources(Map<String, Object> filter) {
        return resourceDao.find(filter);
    }

    /**
     * 启用应用资源
     *
     * @param serviceInfo 应用信息
     */
    @SuppressWarnings("unchecked")
    public void enabledApplicationResource(ServiceInfo serviceInfo) {

        if (serviceInfo == null || serviceInfo.getVersion() == null) {
            return;
        }

        // 应用名称
        String applicationName = serviceInfo.getService();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("开始绑定[" + applicationName + "]资源信息，当前版本为:" + serviceInfo.getVersion());
        }

        Map<String, Object> filter = new HashMap<>(16);

        filter.put("applicationNameEq", applicationName);
        filter.put("sourceContains", Resource.DEFAULT_ALL_SOURCE_VALUES);
        // 获取一次旧的资源，在递归完最新的资源后，把所有未匹配旧的资源全部删除掉
        List<Resource> oldResourceList = findResources(filter);

        List<Map<String, Object>> pluginList = Casts.cast(
                serviceInfo.getInfo().get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME),
                List.class
        );

        // 遍历新资源，更新 serviceInfo 相关的资源信息
        List<Resource> newResourceList = pluginList
                .stream()
                .flatMap(p -> createResource(p, serviceInfo))
                .flatMap(this::enabledApplicationResource)
                .distinct()
                .collect(Collectors.toList());

        // 遍历旧资源，将没用的删除掉
        oldResourceList
                .stream()
                .filter(resource -> !newResourceList.contains(resource))
                .map(Resource::getId)
                .forEach(this::deleteResource);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("绑定[" + applicationName + "]资源信息完成");
        }

        deleteAuthorizationCache();

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
        saveResource(target);

        List<Resource> result = resource.getChildren()
                .stream()
                .map(c -> (Resource) c)
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
        Resource target = getResourceByFilter(resource.getUniqueFilter());

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

            // 如果数据库存在记录，将resource数据替换一次数据获取出来的值
            BeanUtils.copyProperties(
                    resource,
                    target,
                    IdEntity.DEFAULT_ID_NAME,
                    PluginInfo.DEFAULT_CHILDREN_NAME,
                    PluginInfo.DEFAULT_SOURCE_NAME
            );
        }

        return target;
    }

    /**
     * 创建资源
     *
     * @param entry       插件信息
     * @param serviceInfo 服务系你想
     * @return 流集合
     */
    private Stream<Resource> createResource(Map<String, Object> entry, ServiceInfo serviceInfo) {

        PluginInfo plugin = Casts.castMapToObject(
                entry,
                PluginInfo.class,
                PluginInfo.DEFAULT_CHILDREN_NAME
        );

        return Arrays.stream(StringUtils.split(plugin.getSource(), ",")).map(source -> {
            Resource target = new Resource();

            BeanUtils.copyProperties(plugin, target);

            target.setSource(ResourceSource.All.toString().equals(source) ? ResourceSource.Console.toString() : source);

            List<Map<String, Object>> children = Casts.castIfNotNull(entry.get(PluginInfo.DEFAULT_CHILDREN_NAME));
            // 设置 target 变量的子节点
            children.stream()
                    .flatMap(c -> createResource(c, serviceInfo))
                    .forEach(c -> target.getChildren().add(c));

            if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
                target.setParentId(null);
            }

            if (StringUtils.isEmpty(target.getApplicationName())) {
                target.setApplicationName(serviceInfo.getService());
            }

            if (serviceInfo.getVersion() != null) {
                target.setVersion(serviceInfo.getVersion().toString());
            }

            target.setCode(plugin.getId());
            target.setStatus(DisabledOrEnabled.Enabled.getValue());
            target.setId(null);

            return target;
        });
    }

    /**
     * 禁用资源
     *
     * @param applicationName 资源名称
     */
    public void disabledApplicationResource(String applicationName) {
        Map<String, Object> filter = new LinkedHashMap<>();

        filter.put("applicationNameEq", applicationName);
        filter.put("sourceContains", Resource.DEFAULT_ALL_SOURCE_VALUES);
        filter.put("statusEq", DisabledOrEnabled.Enabled.getValue());

        // 查询所有符合条件的资源,并设置为禁用状态
        findResources(filter)
                .stream()
                .peek(resource -> resource.setStatus(DisabledOrEnabled.Disabled.getValue()))
                .forEach(this::saveResource);

        deleteAuthorizationCache();

    }

    /**
     * 所有删除认证缓存
     */
    private void deleteAuthorizationCache() {
        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                "*",
                null,
                ResourceSource.Console.toString()
        );

        UserDetailsService userDetailsService = getUserDetailsService(ResourceSource.Console);

        Set<String> keys = redisTemplate.keys(userDetailsService.getAuthorizationCacheName(token));

        if (CollectionUtils.isNotEmpty(keys)) {
            // 删除所有授权缓存
            redisTemplate.delete(keys);
        }
    }

}
