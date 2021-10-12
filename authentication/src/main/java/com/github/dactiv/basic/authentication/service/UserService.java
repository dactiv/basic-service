package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.dao.ConsoleUserDao;
import com.github.dactiv.basic.authentication.dao.MemberUserDao;
import com.github.dactiv.basic.authentication.dao.MemberUserInitializationDao;
import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.entity.MemberUser;
import com.github.dactiv.basic.authentication.entity.MemberUserInitialization;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.dactiv.basic.commons.Constants.SOCKET_RESULT_ID;
import static com.github.dactiv.framework.spring.security.enumerate.ResourceSource.Console;
import static com.github.dactiv.framework.spring.security.enumerate.ResourceSource.UserCenter;

/**
 * 认证管理服务
 *
 * @author maurice
 */
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class UserService implements InitializingBean {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ConsoleUserDao consoleUserDao;

    @Autowired
    private MemberUserDao memberUserDao;

    @Autowired
    private MemberUserInitializationDao memberUserInitializationDao;

    @Autowired
    private SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationConfig applicationConfig;

    /**
     * 获取授权管理服务
     *
     * @return 授权管理服务
     */
    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    // -------------------------------- 系统用户管理 -------------------------------- //

    /**
     * 保存系统用户
     *
     * @param consoleUser 系统用户实体
     */
    public void saveConsoleUser(ConsoleUser consoleUser) {
        if (Objects.isNull(consoleUser.getId())) {
            insertConsoleUser(consoleUser);
        } else {
            updateConsoleUser(consoleUser);
        }
    }

    /**
     * 保存系统用户
     *
     * @param entity   用户实体
     * @param groupIds 对应组的主键值
     */
    public void saveConsoleUser(ConsoleUser entity, List<Integer> groupIds) {

        saveConsoleUser(entity);

        if (groupIds != null) {

            boolean isAllConsoleGroup = groupIds
                    .stream()
                    .map(authorizationService::getGroup)
                    .flatMap(g -> Arrays.stream(StringUtils.split(g.getSource(), SpringMvcUtils.COMMA_STRING)))
                    .anyMatch(s -> !StringUtils.equals(s, Console.toString()));

            if (isAllConsoleGroup) {
                throw new ServiceException("当前存在不是[" + Console.getName() + "]的信息，无法保存用户");
            }

            consoleUserDao.deleteGroupAssociation(entity.getId());

            if (!groupIds.isEmpty()) {
                consoleUserDao.insertGroupAssociation(entity.getId(), groupIds);
            }
        }

        expireUserSession(entity.getUsername());
    }

    /**
     * 保存系统用户
     *
     * @param entity      用户实体
     * @param groupIds    对应组的主键值
     * @param resourceIds 对应资源主键值
     */
    @SocketMessage(SOCKET_RESULT_ID)
    public void saveConsoleUser(ConsoleUser entity, List<Integer> groupIds, List<Integer> resourceIds) {

        saveConsoleUser(entity, groupIds);

        if (resourceIds != null) {
            consoleUserDao.deleteResourceAssociation(entity.getId());

            if (!resourceIds.isEmpty()) {
                consoleUserDao.insertResourceAssociation(entity.getId(), resourceIds);
            }
        }

        expireUserSession(entity.getUsername());

        SocketResultHolder.get().addBroadcastSocketMessage(ConsoleUser.SAVE_SOCKET_EVENT_NAME, entity);
    }

    /**
     * 新增系统用户
     *
     * @param consoleUser 系统用户实体
     */
    public void insertConsoleUser(ConsoleUser consoleUser) {

        if (StringUtils.isEmpty(consoleUser.getPassword())) {
            throw new ServiceException("登陆密码不能为空");
        }

        ConsoleUser entity = consoleUserDao.selectOne(
                Wrappers
                        .<ConsoleUser>lambdaQuery()
                        .eq(ConsoleUser::getUsername, consoleUser.getUsername())
        );

        if (entity != null) {
            throw new ServiceException("登陆账户【" + consoleUser.getUsername() + "】已存在");
        }

        PasswordEncoder passwordEncoder = authorizationService.getUserDetailsService(Console).getPasswordEncoder();

        String encodePassword = passwordEncoder.encode(consoleUser.getPassword());

        consoleUser.setPassword(encodePassword);

        consoleUserDao.insert(consoleUser);
    }

    /**
     * 更新系统用户
     *
     * @param consoleUser 系统用户实体
     */
    public void updateConsoleUser(ConsoleUser consoleUser) {
        consoleUserDao.updateById(consoleUser);

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(consoleUser.getUsername(), null),
                Console.toString()
        );

        deleteRedisCache(Console, token);

    }

    private void deleteRedisCache(ResourceSource source, PrincipalAuthenticationToken token) {

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(source);

        deleteRedisCache(userDetailsService, token);
    }

    public void deleteRedisCache(UserDetailsService userDetailsService, PrincipalAuthenticationToken token) {
        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        if (Objects.nonNull(authenticationCache)) {

            redissonClient.getBucket(authenticationCache.getName()).deleteAsync();
        }

        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token);

        if (Objects.nonNull(authorizationCache)) {

            redissonClient.getBucket(authorizationCache.getName()).deleteAsync();
        }
    }

    /**
     * 更新系统用户登陆密码
     *
     * @param id          系统用户主键 ID
     * @param oldPassword 旧登陆密码
     * @param newPassword 新登陆密码
     */
    public void updateConsoleUserPassword(Integer id, String oldPassword, String newPassword) {

        ConsoleUser consoleUser = getConsoleUser(id);

        updateConsoleUserPassword(consoleUser, oldPassword, newPassword);
    }

    /**
     * 更新系统用户登陆密码
     *
     * @param consoleUser 系统用户实体
     * @param oldPassword 旧登陆密码
     * @param newPassword 新登陆密码
     */
    public void updateConsoleUserPassword(ConsoleUser consoleUser, String oldPassword, String newPassword) {

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(Console);

        PasswordEncoder passwordEncoder = userDetailsService.getPasswordEncoder();

        if (!passwordEncoder.matches(oldPassword, consoleUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        consoleUserDao.update(
                consoleUser,
                Wrappers.<ConsoleUser>lambdaUpdate()
                        .set(ConsoleUser::getPassword, passwordEncoder.encode(newPassword))
                        .eq(ConsoleUser::getId, consoleUser.getId())
        );

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(consoleUser.getUsername(), null),
                Console.toString()
        );

        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        if (Objects.nonNull(authenticationCache)) {
            redissonClient.getBucket(authenticationCache.getName()).deleteAsync();
        }

        expireUserSession(consoleUser.getUsername());
    }

    /**
     * 将用户的所有 session 设置为超时
     *
     * @param user 用户实体
     */
    private void expireUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    /**
     * 删除系统用户
     *
     * @param ids 主键 id 集合
     */
    @SocketMessage
    public void deleteConsoleUsers(List<Integer> ids) {
        ids.forEach(this::deleteConsoleUser);
    }

    /**
     * 删除系统用户
     *
     * @param id 主键 id
     */
    @SocketMessage
    public void deleteConsoleUser(Integer id) {
        ConsoleUser consoleUser = getConsoleUser(id);
        deleteConsoleUser(consoleUser);
    }

    /**
     * 删除系统用户
     *
     * @param consoleUser 用户实体
     */
    @SocketMessage
    public void deleteConsoleUser(ConsoleUser consoleUser) {
        if (consoleUser == null) {
            return;
        }

        if (StringUtils.equals(consoleUser.getUsername(), applicationConfig.getAdminUsername())) {
            throw new ServiceException("不能删除超级管理员用户");
        }

        consoleUserDao.deleteGroupAssociation(consoleUser.getId());
        consoleUserDao.deleteResourceAssociation(consoleUser.getId());
        consoleUserDao.deleteById(consoleUser.getId());

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(consoleUser.getUsername(), null),
                Console.toString()
        );


        deleteRedisCache(Console, token);

        expireUserSession(token.getType() + ":" + token.getPrincipal().toString());

        SocketResultHolder.get().addBroadcastSocketMessage(ConsoleUser.DELETE_SOCKET_EVENT_NAME, consoleUser.getId());
    }

    /**
     * 获取系统用户
     *
     * @param id 主键 id
     *
     * @return 系统用户实体
     */
    public ConsoleUser getConsoleUser(Integer id) {
        return consoleUserDao.selectById(id);
    }

    /**
     * 获取系统用户
     *
     * @param username 登陆账户
     *
     * @return 系统用户实体
     */
    public ConsoleUser getConsoleUserByUsername(String username) {
        return consoleUserDao.selectOne(Wrappers.<ConsoleUser>lambdaQuery().eq(ConsoleUser::getUsername, username));
    }

    /**
     * 获取系统用户集合
     *
     * @param wrapper 包装器
     *
     * @return 系统用户集合
     */
    public List<ConsoleUser> findConsoleUsers(Wrapper<ConsoleUser> wrapper) {
        return consoleUserDao.selectList(wrapper);
    }

    /**
     * 查找系统用户分页信息
     *
     * @param pageRequest 分页请求
     * @param wrapper     过滤条件
     *
     * @return 分页实体
     */
    public Page<ConsoleUser> findConsoleUserPage(PageRequest pageRequest, Wrapper<ConsoleUser> wrapper) {

        PageDto<ConsoleUser> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<ConsoleUser> result = consoleUserDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 通过用户组查询系统用户集合
     *
     * @param groupId 组 id
     *
     * @return 系统用户集合
     */
    public List<ConsoleUser> findConsoleUserByGroupId(String groupId) {
        return consoleUserDao.findByGroupId(groupId);
    }

    // -------------------------------- 会员用户管理 -------------------------------- //

    /**
     * 保存会员用户
     *
     * @param memberUser 会员用户实体
     */
    public void saveMemberUser(MemberUser memberUser) {
        if (Objects.isNull(memberUser.getId())) {
            insertMemberUser(memberUser);
        } else {
            updateMemberUser(memberUser);
        }
    }

    /**
     * 保存会员用户
     *
     * @param entity   用户实体
     * @param groupIds 对应组的主键值
     */
    public void saveMemberUser(MemberUser entity, List<Integer> groupIds) {

        saveMemberUser(entity);

        boolean isAllMemberGroup = groupIds
                .stream()
                .map(authorizationService::getGroup)
                .flatMap(g -> Arrays.stream(StringUtils.split(g.getSource(), SpringMvcUtils.COMMA_STRING)))
                .anyMatch(s -> !StringUtils.equals(s, UserCenter.toString()));

        if (isAllMemberGroup) {
            throw new ServiceException("当前存在不是[" + UserCenter.getName() + "]的信息，无法保存用户");
        }

        memberUserDao.deleteGroupAssociation(entity.getId());

        if (!groupIds.isEmpty()) {

            memberUserDao.insertGroupAssociation(entity.getId(), groupIds);
        }
    }


    /**
     * 新增会员用户
     *
     * @param memberUser 会员用户实体
     */
    public void insertMemberUser(MemberUser memberUser) {

        if (StringUtils.isEmpty(memberUser.getPassword())) {
            throw new ServiceException("登陆密码不能为空");
        }

        if (memberUserDao.selectOne(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getUsername, memberUser.getUsername())) != null) {
            throw new ServiceException("登陆账户【" + memberUser.getUsername() + "】已存在");
        }

        if (memberUserDao.selectOne(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getPhone, memberUser.getPhone())) != null) {
            throw new ServiceException("手机号码【" + memberUser.getPhone() + "】已存在");
        }

        if (memberUserDao.selectOne(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getEmail, memberUser.getEmail())) != null) {
            throw new ServiceException("邮箱号码【" + memberUser.getEmail() + "】已存在");
        }

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(UserCenter);

        String encodePassword = userDetailsService.getPasswordEncoder().encode(memberUser.getPassword());

        memberUser.setPassword(encodePassword);

        memberUserDao.insert(memberUser);

        MemberUserInitialization memberUserInitialization = new MemberUserInitialization();

        memberUserInitialization.setUserId(memberUser.getId());

        insertMemberUserInitialization(memberUserInitialization);
    }

    /**
     * 更新会员用户
     *
     * @param memberUser 会员用户实体
     */
    public void updateMemberUser(MemberUser memberUser) {
        memberUserDao.updateById(memberUser);

        deleteMemberUserAuthenticationCache(memberUser);

        expireUserSession(memberUser.getUsername());
    }

    /**
     * 更新会员用户登陆密码
     *
     * @param id          会员用户主键 ID
     * @param oldPassword 旧登陆密码
     * @param newPassword 新登陆密码
     */
    public void updateMemberUserPassword(Integer id, String oldPassword, String newPassword) {

        MemberUser memberUser = getMemberUser(id);

        MemberUserInitialization initialization = getMemberUserInitialization(id);

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(UserCenter);

        if (YesOrNo.No.getValue().equals(initialization.getModifyPassword())) {

            if (!passwordEncoder.matches(oldPassword, memberUser.getPassword())) {
                throw new ServiceException("旧密码不正确");
            }

        } else {
            initialization.setModifyPassword(YesOrNo.Yes.getValue());
            updateMemberUserInitialization(initialization);
        }

        memberUser.setPassword(userDetailsService.getPasswordEncoder().encode(newPassword));

        memberUserDao.update(
                memberUser,
                Wrappers
                        .<MemberUser>lambdaUpdate()
                        .set(MemberUser::getPassword, memberUser.getPassword())
                        .eq(MemberUser::getId, memberUser.getId())
        );

        deleteMemberUserAuthenticationCache(memberUser);

        expireUserSession(memberUser.getUsername());
    }

    /**
     * 删除会员用户认证缓存
     *
     * @param memberUser 会员用户信息
     */
    private void deleteMemberUserAuthenticationCache(MemberUser memberUser) {
        authorizationService.getUserDetailsServices().forEach(s -> {
            s.getType().forEach(t -> {
                PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                        new UsernamePasswordAuthenticationToken(memberUser.getUsername(), null),
                        t
                );
                CacheProperties cache = s.getAuthenticationCache(token);

                if (Objects.nonNull(cache)) {
                    redissonClient.getBucket(cache.getName()).deleteAsync();
                }

            });
        });
    }

    /**
     * 更新会员账户登录账户
     *
     * @param id          会员用户主键 ID
     * @param newUsername 新的用户名
     */
    public void updateMemberUserUsername(Integer id, String newUsername) {

        MemberUser memberUser = getMemberUser(id);

        MemberUserInitialization initialization = getMemberUserInitialization(id);

        if (YesOrNo.Yes.getValue().equals(initialization.getModifyUsername())) {
            throw new ServiceException("不能多次修改登录账户");
        }

        MemberUser exist = memberUserDao.selectOne(
                Wrappers
                        .<MemberUser>lambdaQuery()
                        .eq(MemberUser::getUsername, newUsername)
        );

        if (exist != null) {
            throw new ServiceException("登录账户[" + newUsername + "]已存在");
        }

        memberUserDao.update(
                memberUser,
                Wrappers
                        .<MemberUser>lambdaUpdate()
                        .set(MemberUser::getUsername, newUsername)
                        .eq(MemberUser::getId, id)
        );

        initialization.setModifyUsername(YesOrNo.Yes.getValue());

        updateMemberUserInitialization(initialization);

        updateMemberUser(memberUser);

        deleteMemberUserAuthenticationCache(memberUser);
    }

    /**
     * 获取会员用户
     *
     * @param id 主键 id
     *
     * @return 会员用户实体
     */
    public MemberUser getMemberUser(Integer id) {

        MemberUser memberUser = memberUserDao.selectById(id);

        memberUser.setInitialization(getMemberUserInitialization(id));

        return memberUser;

    }

    /**
     * 根据唯一识别获取会员用户
     *
     * @param identified 唯一识别
     *
     * @return 会员用户
     */
    public MemberUser getMemberUserByIdentified(String identified) {
        return memberUserDao.selectOne(
                Wrappers.
                        <MemberUser>lambdaQuery()
                        .eq(MemberUser::getUsername, identified)
                        .or()
                        .eq(MemberUser::getPhone, identified)
                        .or().eq(MemberUser::getEmail, identified)
        );
    }

    /**
     * 获取会员用户集合
     *
     * @param wrapper 包装器
     *
     * @return 会员用户集合
     */
    public List<MemberUser> findMemberUsers(Wrapper<MemberUser> wrapper) {
        return memberUserDao.selectList(wrapper);
    }

    /**
     * 查找会员用户分页信息
     *
     * @param pageRequest 分页信息信息
     * @param wrapper     条件
     *
     * @return 分页实体
     */
    public Page<MemberUser> findMemberUserPage(PageRequest pageRequest, Wrapper<MemberUser> wrapper) {

        PageDto<MemberUser> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<MemberUser> result = memberUserDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    // -------------------------------- 会员用户初始化信息管理 -------------------------------- //

    /**
     * 新增用户初始化实体
     *
     * @param memberUserInitialization 用户初始化实体
     */
    public void insertMemberUserInitialization(MemberUserInitialization memberUserInitialization) {
        memberUserInitializationDao.insert(memberUserInitialization);
    }

    /**
     * 更新用户初始化实体
     *
     * @param memberUserInitialization 用户初始化实体
     */
    public void updateMemberUserInitialization(MemberUserInitialization memberUserInitialization) {

        memberUserInitializationDao.updateById(memberUserInitialization);
        getMemberUserInitializationBucket(memberUserInitialization.getUserId()).deleteAsync();

    }

    /**
     * 获取用户初始化实体
     *
     * @param userId 用户 id
     *
     * @return 用户初始化实体
     */
    public MemberUserInitialization getMemberUserInitialization(Integer userId) {

        RBucket<MemberUserInitialization> bucket = getMemberUserInitializationBucket(userId);

        MemberUserInitialization value = bucket.get();

        if (Objects.nonNull(value)) {
            return value;
        }

        MemberUserInitialization initialization = memberUserInitializationDao.selectOne(
                Wrappers
                        .<MemberUserInitialization>lambdaQuery()
                        .eq(MemberUserInitialization::getUserId, userId)
        );

        if (initialization != null) {
            bucket.setAsync(initialization);
        }

        return initialization;
    }

    /**
     * 获取会员用户初始化桶
     *
     * @param userId 用户 id
     *
     * @return 会员用户初始化桶
     */
    public RBucket<MemberUserInitialization> getMemberUserInitializationBucket(Integer userId) {
        return redissonClient.getBucket(applicationConfig.getMemberUserInitializationCache().getName(userId));
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
        String key = getAnonymousUserKeyPrefix();
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
