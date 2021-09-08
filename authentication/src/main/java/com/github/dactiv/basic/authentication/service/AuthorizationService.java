package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.GroupDao;
import com.github.dactiv.basic.authentication.dao.ResourceDao;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private RequestAuthenticationProvider authenticationProvider;

    @Autowired
    private RedissonClient redissonClient;

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
                source.toString()
        );

        return authenticationProvider
                .getUserDetailsService(token)
                .orElseThrow(() -> new ServiceException("找不到类型为 [" + source + "] 的 UserDetailsService 实现"));
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

        if (Arrays.stream(StringUtils.split(group.getSource(), SpringMvcUtils.COMMA_STRING))
                .anyMatch(s -> !resourceSourceStream.contains(ResourceSource.valueOf(s)))) {
            throw new ServiceException("组来源[" + group.getSource() + "]不在 " + resourceSourceStream + " 范围内");
        }

        saveGroup(group);

        if (resourceIds != null) {

            boolean isNotAnyMatchResourceSource = resourceIds
                    .stream()
                    .map(this::getResource)
                    .noneMatch(r -> isAdminGroupSource(group, r.getSource()));

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
        Group entity = groupDao.selectOne(
                Wrappers.
                        <Group>lambdaQuery()
                        .eq(Group::getName, group.getName())
        );

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

        if (YesOrNo.No.getValue().equals(group.getModifiable())) {
            throw new ServiceException("用户组【" + group.getName() + "】不可修改");
        }

        groupDao.updateById(group);

        deleteAuthorizationCache();

    }

    /**
     * 增加组資源
     *
     * @param group 组实体
     * @param resourceList 新增的資源 id
     */
    public void addGroupResource(Group group, List<Resource> resourceList) {

        if (CollectionUtils.isEmpty(resourceList)) {
            return ;
        }

        boolean isNotAnyMatchResourceSource = resourceList
                .stream()
                .anyMatch(r -> !isAdminGroupSource(group, r.getSource()));

        if (isNotAnyMatchResourceSource) {
            throw new ServiceException("当前存在关联着不属于 " + group.getSourceName() + " 的资源");
        }

        groupDao.insertResourceAssociation(
                group.getId(),
                resourceList.stream().map(Resource::getId).collect(Collectors.toList())
        );
    }

    /**
     * 获取组来源
     *
     * @param group 组
     *
     * @return 组来源集合
     */
    public List<String> getGroupSources(Group group) {
        Stream<String> sourceStream = Arrays.stream(StringUtils.split(group.getSource(), SpringMvcUtils.COMMA_STRING));

        return sourceStream
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * 是否管理后台来源
     *
     * @param group 组
     * @param source 来源
     * @return true 是，否则 false
     */
    public boolean isAdminGroupSource(Group group, String source) {
        List<String> sources = getGroupSources(group);

        return sources.contains(source) || Resource.DEFAULT_CONTAIN_SOURCE_VALUES.contains(source);
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

        if (YesOrNo.No.getValue().equals(group.getRemovable())) {
            throw new ServiceException("用户组【" + group.getName() + "】不可删除");
        }

        List<Group> groups = findGroups(Wrappers.<Group>lambdaQuery().eq(Group::getParentId, id));

        groups.forEach(g -> deleteGroup(g.getId()));

        groupDao.deleteResourceAssociation(id);
        groupDao.deleteConsoleUserAssociation(id);
        groupDao.deleteMemberUserAssociation(id);
        groupDao.deleteById(id);

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
        return groupDao.selectById(id);
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
     * 根据过滤条件查找用户组
     *
     * @param wrapper 包装器
     * @return 用户组实体集合
     */
    public List<Group> findGroups(Wrapper<Group> wrapper) {
        return groupDao.selectList(wrapper);
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

        if (resourceDao.selectOne(resource.getUniqueWrapper()) != null) {
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
        resourceDao.updateById(resource);

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

        List<Resource> resources = resourceDao.selectList(
                Wrappers
                        .<Resource>lambdaQuery()
                        .eq(Resource::getParentId, id)
        );

        resources.forEach(r -> deleteResource(r.getId()));

        resourceDao.deleteConsoleUserAssociation(id);
        resourceDao.deleteGroupAssociation(id);
        resourceDao.deleteById(id);

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
        return resourceDao.selectById(id);
    }

    /**
     * 通过条件查询单个資源实体
     *
     * @param wrapper 查询条件
     * @return 資源实体
     */
    public Resource fineOneResource(Wrapper<Resource> wrapper) {
        return resourceDao.selectOne(wrapper);
    }


    /**
     * 获取资源
     *
     * @param groupId 组主键值
     * @return 资源实体集合
     */
    public List<Resource> getGroupResources(Integer groupId, String applicationName) {
        return resourceDao.getGroupResources(groupId, applicationName);
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
    public List<Resource> getConsolePrincipalResources(Integer userId, List<String> sourceContains, String type) {
        return resourceDao.getConsolePrincipalResources(userId, sourceContains, type);
    }

    /**
     * 获取資源集合
     *
     * @param wrapper 包装器
     * @return 資源集合
     */
    public List<Resource> findResources(Wrapper<Resource> wrapper) {
        return resourceDao.selectList(wrapper);
    }

    /**
     * 所有删除认证缓存
     */
    public void deleteAuthorizationCache() {
        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken("*", null),
                ResourceSource.Console.toString()
        );

        UserDetailsService userDetailsService = getUserDetailsService(ResourceSource.Console);

        CacheProperties cache = userDetailsService.getAuthorizationCache(token);

        redissonClient.getBucket(cache.getName()).deleteAsync();
    }
}
