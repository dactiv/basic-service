package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.ConsoleUserDao;
import com.github.dactiv.basic.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.basic.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * tb_console_user 的业务逻辑
 *
 * <p>Table: tb_console_user - 后台用户表</p>
 *
 * @author maurice.chen
 * @see ConsoleUserEntity
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsoleUserService extends BasicService<ConsoleUserDao, ConsoleUserEntity> {

    private final AuthorizationService authorizationService;

    public ConsoleUserService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * 更新后台用户密码
     *
     * @param userId      用户 id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void updateConsoleUserPassword(Integer userId, String oldPassword, String newPassword) {
        ConsoleUserEntity consoleUser = get(userId);

        PasswordEncoder passwordEncoder = authorizationService
                .getUserDetailsService(ResourceSourceEnum.CONSOLE)
                .getPasswordEncoder();

        if (!StringUtils.equals(passwordEncoder.encode(oldPassword), consoleUser.getPassword())) {
            throw new ServiceException("旧密码不正确");
        }

        ConsoleUserEntity user = consoleUser.ofIdData();
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);

        authorizationService.expireSystemUserSession(consoleUser.getUsername());
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {

        Wrapper<ConsoleUserEntity> wrapper = Wrappers
                .<ConsoleUserEntity>lambdaQuery()
                .select(SystemUserEntity::getUsername)
                .in(SystemUserEntity::getId, ids);

        List<ConsoleUserEntity> users = find(wrapper);

        users
                .stream()
                .flatMap(this::createPrincipalAuthenticationTokenStream)
                .peek(p -> authorizationService.deleteSystemUseAuthenticationCache(ResourceSourceEnum.CONSOLE, p))
                .forEach(p -> authorizationService.expireSystemUserSession(p.getName()));

        return super.deleteById(ids, errorThrow);
    }

    /**
     * 创建授权 token 流
     *
     * @param user 后台用户实体
     *
     * @return 授权 token 流
     */
    private Stream<PrincipalAuthenticationToken> createPrincipalAuthenticationTokenStream(ConsoleUserEntity user) {
        return Stream.of(
                new PrincipalAuthenticationToken(user.getUsername(), ResourceSourceEnum.CONSOLE.toString()),
                new PrincipalAuthenticationToken(user.getEmail(), ResourceSourceEnum.CONSOLE.toString())
        );
    }

    /**
     * 通过登录账号或邮箱获取后台用户
     *
     * @param identity 登录账号或邮箱
     *
     * @return 后台用户
     */
    public ConsoleUserEntity getByIdentity(String identity) {
        return lambdaQuery()
                .eq(SystemUserEntity::getUsername, identity)
                .or()
                .eq(SystemUserEntity::getEmail, identity)
                .one();
    }
}
