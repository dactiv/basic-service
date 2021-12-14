package com.github.dactiv.basic.authentication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.authentication.domain.entity.GroupEntity;
import com.github.dactiv.basic.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.basic.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.basic.authentication.plugin.PluginResourceService;
import com.github.dactiv.basic.commons.authentication.IdRoleAuthority;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.ResourceAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

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
public class AuthorizationService implements InitializingBean {

    private final RequestAuthenticationProvider authenticationProvider;

    private final RedissonClient redissonClient;

    private final PasswordEncoder passwordEncoder;

    private final GroupService groupService;

    @Getter
    private final PluginResourceService pluginResourceService;

    private final SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;

    public AuthorizationService(RequestAuthenticationProvider authenticationProvider,
                                RedissonClient redissonClient,
                                PluginResourceService pluginResourceService,
                                PasswordEncoder passwordEncoder,
                                GroupService groupService,
                                SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry) {
        this.authenticationProvider = authenticationProvider;
        this.redissonClient = redissonClient;
        this.pluginResourceService = pluginResourceService;
        this.passwordEncoder = passwordEncoder;
        this.groupService = groupService;
        this.sessionBackedSessionRegistry = sessionBackedSessionRegistry;
    }

    /**
     * 将用户的所有 session 设置为超时
     *
     * @param user 用户实体
     */
    public void expireSystemUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param source 资源累袁
     *
     * @return 账户认证的用户明细服务
     */
    public UserDetailsService getUserDetailsService(ResourceSourceEnum source) {

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                "",
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


    /**
     * 获取组资源集合
     *
     * @param group 组信息
     *
     * @return 资源结婚
     */
    public List<ResourceMeta> getGroupResource(GroupEntity group) {
        List<ResourceMeta> result = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry: group.getResourceMap().entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());
            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .collect(Collectors.toList());

            result.addAll(findResources);
        }

        return result;
    }

    /**
     * 所有删除认证缓存
     */
    public void deleteAuthorizationCache(List<ResourceSourceEnum> sources) {
        List<PrincipalAuthenticationToken> tokens = sources
                .stream()
                .map(s -> new PrincipalAuthenticationToken("*", s.toString()))
                .collect(Collectors.toList());

        for (PrincipalAuthenticationToken token : tokens) {
            UserDetailsService userDetailsService = getUserDetailsService(ResourceSourceEnum.CONSOLE);
            CacheProperties cache = userDetailsService.getAuthorizationCache(token);
            redissonClient.getBucket(cache.getName()).deleteAsync();
        }
    }

    /**
     * 获取资源集合
     *
     * @param applicationName 应用名称
     * @param sources         符合来源的记录
     *
     * @return 资源集合
     */
    public List<ResourceMeta> getResources(String applicationName, ResourceSourceEnum... sources) {
        List<ResourceMeta> result = pluginResourceService.getResources();
        Stream<ResourceMeta> stream = result.stream();

        if (StringUtils.isNotBlank(applicationName)) {
            stream = stream.filter(r -> r.getApplicationName().equals(applicationName));
        }

        if (ArrayUtils.isNotEmpty(sources)) {
            List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
            stream = stream.filter(r -> r.getSources().stream().anyMatch(sourceList::contains));
        }

        return stream.sorted(Comparator.comparing(ResourceMeta::getSort)).collect(Collectors.toList());
    }

    public void deleteSystemUseAuthenticationCache(ResourceSourceEnum source, PrincipalAuthenticationToken token) {

        UserDetailsService userDetailsService = getUserDetailsService(source);

        deleteSystemUseAuthenticationCache(userDetailsService, token);
    }

    public void deleteSystemUseAuthenticationCache(UserDetailsService userDetailsService,
                                                    PrincipalAuthenticationToken token) {
        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        if (Objects.nonNull(authenticationCache)) {

            redissonClient.getBucket(authenticationCache.getName()).deleteAsync();
        }

        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token);

        if (Objects.nonNull(authorizationCache)) {

            redissonClient.getBucket(authorizationCache.getName()).deleteAsync();
        }
    }

    // -------------------------------- 资源管理 -------------------------------- //

    /**
     * 获取系统用户资源
     *
     * @param user 系统用户
     * @param type 资源类型
     * @param sourceContains 资源来源
     *
     * @return 系统用户资源集合
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user,
                                                    ResourceType type,
                                                    List<ResourceSourceEnum> sourceContains) {
        List<ResourceMeta> userResource = getSystemUserResource(user);

        return userResource
                .stream()
                .filter(r -> r.getType().equals(type))
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains))
                .collect(Collectors.toList());
    }

    /**
     * 设置系统用户权限信息
     *
     * @param user 系统用户
     * @param userDetails 当前的安全用户明细
     */
    public void setSystemUserAuthorities(SystemUserEntity user, SecurityUserDetails userDetails) {
        List<IdRoleAuthority> roleAuthorities = Casts.convertValue(user.getGroupsInfo(), new TypeReference<>() {});
        userDetails.getRoleAuthorities().addAll(roleAuthorities);
        // 构造用户的组资源
        List<ResourceMeta> userResource = getSystemUserResource(user);
        // 构造对应 spring security 的资源内容
        List<ResourceAuthority> resourceAuthorities = userResource
                .stream()
                .flatMap(this::createResourceAuthoritiesStream)
                .collect(Collectors.toList());

        userDetails.setResourceAuthorities(resourceAuthorities);
    }

    /**
     * 获取系统用户资源
     *
     * @param user 系统用户
     *
     * @return 系统用户资源
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user) {
        List<IdRoleAuthority> roleAuthorities = user.getGroupsInfo();

        // 通过 id 获取组信息
        List<Integer> groupIds = roleAuthorities
                .stream()
                .map(IdRoleAuthority::getId)
                .collect(Collectors.toList());

        List<GroupEntity> groups = groupService.get(groupIds);

        // 获取组来源，用于过滤组的资源里有存在不同的资源来源细腻些
        List<ResourceSourceEnum> groupSources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .distinct()
                .collect(Collectors.toList());

        // 构造用户的组资源
        List<ResourceMeta> userResource = groups
                .stream()
                .flatMap(g -> getResourcesStream(g.getResourceMap(), groupSources))
                .collect(Collectors.toList());

        // 构造用户的独立资源
        userResource.addAll(getResourcesStream(user.getResourceMap(), groupSources).collect(Collectors.toList()));

        return userResource;
    }

    private Stream<ResourceMeta> getResourcesStream(Map<String, List<String>> resourceMap, List<ResourceSourceEnum> sources) {

        if (MapUtils.isEmpty(resourceMap)) {
            return Stream.empty();
        }

        List<ResourceMeta> result = new LinkedList<>();

        for (Map.Entry<String, List<String>> entry : resourceMap.entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());

            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .filter(r -> r.getSources().stream().anyMatch(sources::contains))
                    .collect(Collectors.toList());

            result.addAll(findResources);
        }

        return result.stream();
    }

    private Stream<ResourceAuthority> createResourceAuthoritiesStream(ResourceMeta resource) {
        if (StringUtils.isBlank(resource.getAuthority())) {
            return Stream.empty();
        }

        String[] permissions = StringUtils.substringsBetween(
                resource.getAuthority(),
                ResourceAuthority.DEFAULT_RESOURCE_PREFIX,
                ResourceAuthority.DEFAULT_RESOURCE_SUFFIX
        );

        if (ArrayUtils.isEmpty(permissions)) {
            return Stream.empty();
        }

        return Arrays
                .stream(permissions)
                .map(ResourceAuthority::getPermissionValue)
                .map(p -> new ResourceAuthority(p, resource.getName(), resource.getValue()));
    }

    @Override
    public void afterPropertiesSet() {

        RBucket<AnonymousUser> bucket = getAnonymousUserBucket();

        AnonymousUser value = bucket.get();

        if (value == null) {

            String password = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
            String md5 = DigestUtils.md5DigestAsHex((password + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME).getBytes());

            AnonymousUser anonymousUser = new AnonymousUser(
                    passwordEncoder.encode(md5),
                    password
            );

            bucket.set(anonymousUser);

        }
    }

    /**
     * 获取匿名用户
     *
     * @return 匿名用户
     */
    public UserDetails getAnonymousUser() {
        return getAnonymousUserBucket().get();
    }

    /**
     * 更新匿名用户密码
     */
    @NacosCronScheduled(cron = "${authentication.anonymous-user.update-password-cron:0 0/30 * * * ?}", name = "更新匿名用户密码")
    public void updateAnonymousUserPassword() {

        String password = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
        String md5 = DigestUtils.md5DigestAsHex((password + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME).getBytes());

        AnonymousUser anonymousUser = new AnonymousUser(
                passwordEncoder.encode(md5),
                password
        );

        getAnonymousUserBucket().set(anonymousUser);
    }

    private String getAnonymousUserKeyPrefix() {
        return UserDetailsService.DEFAULT_AUTHENTICATION_KEY_NAME + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME;
    }

    public RBucket<AnonymousUser> getAnonymousUserBucket() {
        return redissonClient.getBucket(getAnonymousUserKeyPrefix());
    }
}
