package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.GroupDao;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceService;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.dactiv.basic.commons.Constants.WEB_FILTER_RESULT_ID;

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
    private RequestAuthenticationProvider authenticationProvider;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PluginResourceService pluginResourceService;

    /**
     * 超级管理员组 id
     */
    @Value("${spring.security.admin.group-id:1}")
    private Integer adminGroupId;


    /**
     * 获取账户认证的用户明细服务
     *
     * @param source 资源累袁
     *
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
    @SocketMessage(WEB_FILTER_RESULT_ID)
    public void saveGroup(Group group) {

        List<Resource> groupResource = getGroupResource(group);

        List<String> noneMatchSource = groupResource
                .stream()
                .flatMap(r -> r.getSources().stream().filter(s -> !group.getSources().contains(s)))
                .collect(Collectors.toList());

        if (!noneMatchSource.isEmpty()) {
            throw new ServiceException("组来源 [" + group.getSourceName() + "] 不能保存属于 [" + noneMatchSource + "] 的资源");
        }

        if (Objects.isNull(group.getId())) {
            insertGroup(group);
        } else {
            updateGroup(group);
        }
    }

    /**
     * 获取组资源集合
     *
     * @param group 组信息
     *
     * @return 资源结婚
     */
    private List<Resource> getGroupResource(Group group) {
        List<Resource> result = new LinkedList<>();
        for (Map.Entry<String, List<Integer>> entry: group.getResourceMap().entrySet()) {
            List<Resource> resources = pluginResourceService.getResources(entry.getKey());
            List<Resource> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .collect(Collectors.toList());

            result.addAll(findResources);
        }

        return result;
    }

    /**
     * 新增用户组
     *
     * @param group 用户组实体
     */
    @SocketMessage(WEB_FILTER_RESULT_ID)
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

        SocketResultHolder.get().addBroadcastSocketMessage(Group.CREATE_SOCKET_EVENT_NAME, group);
    }

    /**
     * 更新用户组
     *
     * @param group 用户组实体
     */
    @SocketMessage(WEB_FILTER_RESULT_ID)
    public void updateGroup(Group group) {

        if (YesOrNo.No.getValue().equals(group.getModifiable())) {
            throw new ServiceException("用户组【" + group.getName() + "】不可修改");
        }

        groupDao.updateById(group);
        deleteAuthorizationCache();
        SocketResultHolder.get().addBroadcastSocketMessage(Group.UPDATE_SOCKET_EVENT_NAME, group);

    }


    /**
     * 删除用户组
     *
     * @param ids 主键 id 集合
     */
    @SocketMessage(WEB_FILTER_RESULT_ID)
    public void deleteGroup(List<Integer> ids) {
        ids.forEach(this::deleteGroup);
    }

    /**
     * 删除用户组
     *
     * @param id 主键 id
     */
    @SocketMessage(WEB_FILTER_RESULT_ID)
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
        groupDao.deleteById(id);

        if (groups.isEmpty()) {
            deleteAuthorizationCache();
        }

        SocketResultHolder.get().addBroadcastSocketMessage(Group.DELETE_SOCKET_EVENT_NAME, id);
    }

    /**
     * 获取用户组
     *
     * @param id 主键 id
     *
     * @return 用户组实体
     */
    public Group getGroup(Integer id) {
        return groupDao.selectById(id);
    }

    /**
     * 根据过滤条件查找用户组
     *
     * @param wrapper 包装器
     *
     * @return 用户组实体集合
     */
    public List<Group> findGroups(Wrapper<Group> wrapper) {
        return groupDao.selectList(wrapper);
    }

    // -------------------------------- 资源管理 -------------------------------- //

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
