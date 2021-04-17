package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.basic.authentication.dao.ConsoleUserDao;
import com.github.dactiv.basic.authentication.dao.MemberUserDao;
import com.github.dactiv.basic.authentication.dao.MemberUserInitializationDao;
import com.github.dactiv.basic.authentication.dao.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.dao.entity.MemberUser;
import com.github.dactiv.basic.authentication.dao.entity.MemberUserInitialization;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.concurrent.annotation.ConcurrentProcess;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 超级管理登陆账户
     */
    @Value("${spring.security.admin.username:admin}")
    private String adminUsername;

    @Value("${spring.application.member-user.initialization-key-prefix:member:user:initialization:}")
    private String memberUserInitializationKeyPrefix;

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
                    .flatMap(g -> Arrays.stream(StringUtils.split(g.getSource(), ",")))
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
    public void saveConsoleUser(ConsoleUser entity, List<Integer> groupIds, List<Integer> resourceIds) {

        saveConsoleUser(entity, groupIds);

        if (resourceIds != null) {
            consoleUserDao.deleteResourceAssociation(entity.getId());

            if (!resourceIds.isEmpty()) {
                consoleUserDao.insertResourceAssociation(entity.getId(), resourceIds);
            }
        }

        expireUserSession(entity.getUsername());
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

        ConsoleUser entity = getConsoleUserByFilter(consoleUser.getUniqueFilter());

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
        consoleUserDao.update(consoleUser);

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                consoleUser.getUsername(),
                null,
                Console.toString()
        );

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(Console);

        redisTemplate.delete(userDetailsService.getAuthenticationCacheName(token));
        redisTemplate.delete(userDetailsService.getAuthenticationCacheName(token));
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

        checkPassword(passwordEncoder, oldPassword, consoleUser.getPassword());

        consoleUserDao.updatePassword(consoleUser.getId(), passwordEncoder.encode(newPassword));

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                consoleUser.getUsername(),
                null,
                Console.toString()
        );

        redisTemplate.delete(userDetailsService.getAuthenticationCacheName(token));

        expireUserSession(consoleUser.getUsername());
    }

    private void checkPassword(PasswordEncoder passwordEncoder, String assertPassword, String expectPassword) {
        if (!passwordEncoder.matches(assertPassword, expectPassword)) {
            throw new ServiceException("旧密码不正确");
        }
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
    public void deleteConsoleUsers(List<Integer> ids) {
        ids.forEach(this::deleteConsoleUser);
    }

    /**
     * 删除系统用户
     *
     * @param id 主键 id
     */
    public void deleteConsoleUser(Integer id) {
        ConsoleUser consoleUser = getConsoleUser(id);
        deleteConsoleUser(consoleUser);
    }

    /**
     * 删除系统用户
     *
     * @param consoleUser 用户实体
     */
    public void deleteConsoleUser(ConsoleUser consoleUser) {
        if (consoleUser == null) {
            return;
        }

        if (StringUtils.equals(consoleUser.getUsername(), adminUsername)) {
            throw new ServiceException("不能删除超级管理员用户");
        }

        consoleUserDao.deleteGroupAssociation(consoleUser.getId());
        consoleUserDao.deleteResourceAssociation(consoleUser.getId());
        consoleUserDao.delete(consoleUser.getId());

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                consoleUser.getUsername(),
                null,
                Console.toString()
        );

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(Console);

        redisTemplate.delete(userDetailsService.getAuthenticationCacheName(token));
        redisTemplate.delete(userDetailsService.getAuthorizationCacheName(token));

        expireUserSession(token.getType() + ":" + token.getPrincipal().toString());
    }

    /**
     * 获取系统用户
     *
     * @param id 主键 id
     * @return 系统用户实体
     */
    public ConsoleUser getConsoleUser(Integer id) {
        return getConsoleUser(id, false);
    }

    /**
     * 获取系统用户
     *
     * @param id   主键 id
     * @param lock 是否加锁
     * @return 系统用户实体
     */
    public ConsoleUser getConsoleUser(Integer id, Boolean lock) {
        if (lock) {
            return consoleUserDao.lock(id);
        }
        return consoleUserDao.get(id);
    }

    /**
     * 获取系统用户
     *
     * @param filter 登陆账户
     * @return 系统用户实体
     */
    public ConsoleUser getConsoleUserByFilter(Map<String, Object> filter) {

        List<ConsoleUser> result = findConsoleUsers(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<ConsoleUser> iterator = result.iterator();

        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 根据过滤条件查找系统用户
     *
     * @param filter 过滤条件
     * @return 系统用户实体集合
     */
    public List<ConsoleUser> findConsoleUsers(Map<String, Object> filter) {
        return consoleUserDao.find(filter);
    }

    /**
     * 查找系统用户分页信息
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<ConsoleUser> findConsoleUserPage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<ConsoleUser> data = findConsoleUsers(filter);

        return new Page<>(pageRequest, data);
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
                .flatMap(g -> Arrays.stream(StringUtils.split(g.getSource(), ",")))
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

        checkMemberUserData(memberUser, memberUser.getUniqueFilter());

        UserDetailsService userDetailsService = authorizationService.getUserDetailsService(UserCenter);

        String encodePassword = userDetailsService.getPasswordEncoder().encode(memberUser.getPassword());

        memberUser.setPassword(encodePassword);

        memberUserDao.insert(memberUser);

        MemberUserInitialization memberUserInitialization = new MemberUserInitialization();

        memberUserInitialization.setUserId(memberUser.getId());

        insertMemberUserInitialization(memberUserInitialization);
    }

    /**
     * 检查会用用户数据是否正确
     *
     * @param memberUser 会员用户
     * @param filter     条件信息
     */
    private void checkMemberUserData(MemberUser memberUser, Map<String, Object> filter) {

        if (filter.containsKey("usernameEq")) {

            Map<String, Object> testFilter = new LinkedHashMap<>();

            testFilter.put("usernameEq", filter.get("usernameEq"));

            MemberUser exist = getMemberUserByFilter(testFilter);

            if (exist != null) {
                throw new ServiceException("登陆账户【" + memberUser.getUsername() + "】已存在");
            }
        }

        if (filter.containsKey("phoneEq")) {

            Map<String, Object> testFilter = new LinkedHashMap<>();

            testFilter.put("phoneEq", filter.get("phoneEq"));

            MemberUser exist = getMemberUserByFilter(testFilter);

            if (exist != null) {
                throw new ServiceException("手机号码【" + memberUser.getPhone() + "】已存在");
            }
        }

        if (filter.containsKey("emailEq")) {

            Map<String, Object> testFilter = new LinkedHashMap<>();

            testFilter.put("emailEq", filter.get("emailEq"));

            MemberUser exist = getMemberUserByFilter(testFilter);

            if (exist != null) {
                throw new ServiceException("邮箱号码【" + memberUser.getEmail() + "】已存在");
            }
        }

    }

    /**
     * 更新会员用户
     *
     * @param memberUser 会员用户实体
     */
    public void updateMemberUser(MemberUser memberUser) {
        memberUserDao.update(memberUser);

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
            checkPassword(userDetailsService.getPasswordEncoder(), oldPassword, memberUser.getPassword());
        } else {
            initialization.setModifyPassword(YesOrNo.Yes.getValue());
            updateMemberUserInitialization(initialization);
        }

        memberUser.setPassword(userDetailsService.getPasswordEncoder().encode(newPassword));
        memberUserDao.updatePassword(memberUser.getId(), newPassword);

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
                        memberUser.getUsername(),
                        null,
                        t
                );
                redisTemplate.delete(s.getAuthenticationCacheName(token));
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

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("usernameEq", newUsername);

        MemberUser exist = getMemberUserByFilter(filter);

        if (exist != null) {
            throw new ServiceException("登录账户[" + newUsername + "]已存在");
        }

        memberUserDao.updateUsername(id, newUsername);

        initialization.setModifyUsername(YesOrNo.Yes.getValue());

        updateMemberUserInitialization(initialization);

        updateMemberUser(memberUser);

        deleteMemberUserAuthenticationCache(memberUser);
    }

    /**
     * 获取会员用户
     *
     * @param id 主键 id
     * @return 会员用户实体
     */
    public MemberUser getMemberUser(Integer id) {

        MemberUser memberUser = getMemberUser(id, false);

        memberUser.setInitialization(getMemberUserInitialization(id));

        return memberUser;

    }

    /**
     * 获取会员用户
     *
     * @param id   主键 id
     * @param lock 是否加锁
     * @return 会员用户实体
     */
    public MemberUser getMemberUser(Integer id, Boolean lock) {
        if (lock) {
            return memberUserDao.lock(id);
        }
        return memberUserDao.get(id);
    }

    /**
     * 获取会员用户
     *
     * @param filter 登陆账户
     * @return 会员用户实体
     */
    public MemberUser getMemberUserByFilter(Map<String, Object> filter) {

        List<MemberUser> result = findMemberUsers(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<MemberUser> iterator = result.iterator();

        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 根据唯一识别获取会员用户
     *
     * @param identified 唯一识别
     * @return 会员用户
     */
    public MemberUser getMemberUserByIdentified(String identified) {
        return memberUserDao.getByIdentified(identified);
    }

    /**
     * 根据过滤条件查找会员用户
     *
     * @param filter 过滤条件
     * @return 会员用户实体集合
     */
    public List<MemberUser> findMemberUsers(Map<String, Object> filter) {
        return memberUserDao.find(filter);
    }

    /**
     * 查找会员用户分页信息
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<MemberUser> findMemberUserPage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<MemberUser> data = findMemberUsers(filter);

        return new Page<>(pageRequest, data);
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

        memberUserInitializationDao.update(memberUserInitialization);

        String key = getMemberUserInitializationKey(memberUserInitialization.getUserId());
        redisTemplate.delete(key);

    }

    /**
     * 获取用户初始化实体
     *
     * @param userId 用户 id
     * @return 用户初始化实体
     */
    public MemberUserInitialization getMemberUserInitialization(Integer userId) {

        String key = getMemberUserInitializationKey(userId);

        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            return Casts.cast(value);
        }

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("userIdEq", userId);

        List<MemberUserInitialization> result = memberUserInitializationDao.find(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录大于" + result.size() + "条,并非单一记录");
        }

        Iterator<MemberUserInitialization> iterator = result.iterator();

        MemberUserInitialization initialization = iterator.hasNext() ? iterator.next() : null;

        if (initialization != null) {
            redisTemplate.opsForValue().set(key, initialization);
        }

        return initialization;
    }

    private String getMemberUserInitializationKey(Integer userId) {
        return memberUserInitializationKeyPrefix + userId;
    }

    @Override
    @ConcurrentProcess(value = "anonymousUser", exceptionMessage = "生成匿名用户遇到并发，不执行重试操作")
    public void afterPropertiesSet() {

        String key = getAnonymousUserKeyPrefix();

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {

            String password = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
            String md5 = DigestUtils.md5DigestAsHex((password + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME).getBytes());

            AnonymousUser anonymousUser = new AnonymousUser(
                    passwordEncoder.encode(md5),
                    password
            );

            redisTemplate.opsForValue().set(key, anonymousUser);

        }
    }

    /**
     * 获取匿名用户
     *
     * @return 匿名用户
     */
    public UserDetails getAnonymousUser() {
        String key = getAnonymousUserKeyPrefix();
        return Casts.cast(redisTemplate.opsForValue().get(key));
    }

    @ConcurrentProcess(value = "anonymousUser", exceptionMessage = "生成匿名用户遇到并发，不执行重试操作")
    @Scheduled(cron = "${spring.security.anonymous-user.update-password-cron-expression:0 0/30 * * * ?}")
    public void updateAnonymousUserPassword() {

        String password = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
        String md5 = DigestUtils.md5DigestAsHex((password + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME).getBytes());

        AnonymousUser anonymousUser = new AnonymousUser(
                passwordEncoder.encode(md5),
                password
        );

        String key = getAnonymousUserKeyPrefix();

        redisTemplate.opsForValue().set(key, anonymousUser);
    }

    private String getAnonymousUserKeyPrefix() {
        return UserDetailsService.DEFAULT_AUTHENTICATION_KEY_NAME + "anonymousUser";
    }
}
