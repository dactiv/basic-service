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
import com.github.dactiv.framework.security.entity.ResourceAuthority;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
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
 * ??????????????????
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
     * ?????????????????? session ???????????????
     *
     * @param user ????????????
     */
    public void expireSystemUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param source ????????????
     *
     * @return ?????????????????????????????????
     */
    public UserDetailsService getUserDetailsService(ResourceSourceEnum source) {

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                "",
                source.toString()
        );

        return authenticationProvider
                .getUserDetailsService(token)
                .orElseThrow(() -> new ServiceException("?????????????????? [" + source + "] ??? UserDetailsService ??????"));
    }

    /**
     * ?????????????????????
     *
     * @param group ?????????
     *
     * @return ????????????
     */
    public List<ResourceMeta> getGroupResource(GroupEntity group) {
        List<ResourceMeta> result = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : group.getResourceMap().entrySet()) {
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
     * ????????????????????????
     *
     * @param sources ??????????????????
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
     * ??????????????????
     *
     * @param applicationName ????????????
     * @param sources         ?????????????????????
     *
     * @return ????????????
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

    /**
     * ?????????????????????????????????
     *
     * @param source ????????????
     * @param token ?????? token
     */
    public void deleteSystemUseAuthenticationCache(ResourceSourceEnum source, PrincipalAuthenticationToken token) {

        UserDetailsService userDetailsService = getUserDetailsService(source);

        deleteSystemUseAuthenticationCache(userDetailsService, token);
    }

    /**
     * ?????????????????????????????????
     *
     * @param userDetailsService ??????????????????
     * @param token ?????? token
     */
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

    // -------------------------------- ???????????? -------------------------------- //

    /**
     * ????????????????????????
     *
     * @param user           ????????????
     * @param type           ????????????
     * @param sourceContains ????????????
     *
     * @return ????????????????????????
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user,
                                                    ResourceType type,
                                                    List<ResourceSourceEnum> sourceContains) {
        List<ResourceMeta> userResource = getSystemUserResource(user);

        Stream<ResourceMeta> stream = userResource
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));

        if (Objects.nonNull(type)) {
            stream = stream.filter(r -> r.getType().equals(type));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * ??????????????????????????????
     *
     * @param user        ????????????
     * @param userDetails ???????????????????????????
     */
    public void setSystemUserAuthorities(SystemUserEntity user, SecurityUserDetails userDetails) {
        List<IdRoleAuthority> roleAuthorities = Casts.convertValue(user.getGroupsInfo(), new TypeReference<>() {});
        userDetails.getRoleAuthorities().addAll(roleAuthorities);
        // ????????????????????????
        List<ResourceMeta> userResource = getSystemUserResource(user);
        // ???????????? spring security ???????????????
        List<ResourceAuthority> resourceAuthorities = userResource
                .stream()
                .flatMap(this::createResourceAuthoritiesStream)
                .collect(Collectors.toList());

        userDetails.setResourceAuthorities(resourceAuthorities);
    }

    /**
     * ????????????????????????
     *
     * @param user ????????????
     *
     * @return ??????????????????
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user) {
        List<IdRoleAuthority> roleAuthorities = user.getGroupsInfo();

        // ?????? id ???????????????
        List<Integer> groupIds = roleAuthorities
                .stream()
                .map(IdRoleAuthority::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }

        List<GroupEntity> groups = groupService.get(groupIds);

        // ????????????????????????????????????????????????????????????????????????????????????
        List<ResourceSourceEnum> groupSources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .distinct()
                .collect(Collectors.toList());

        // ????????????????????????
        List<ResourceMeta> userResource = groups
                .stream()
                .flatMap(g -> getResourcesStream(g.getResourceMap(), groupSources))
                .collect(Collectors.toList());

        // ???????????????????????????
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
     * ??????????????????
     *
     * @return ????????????
     */
    public UserDetails getAnonymousUser() {
        return getAnonymousUserBucket().get();
    }

    /**
     * ????????????????????????
     */
    @NacosCronScheduled(cron = "${authentication.anonymous-user.update-password-cron:0 0/30 * * * ?}", name = "????????????????????????")
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
