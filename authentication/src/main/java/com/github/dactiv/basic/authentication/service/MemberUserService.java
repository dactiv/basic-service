package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.MemberUserDao;
import com.github.dactiv.basic.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.basic.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_member_user 的业务逻辑
 *
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @see MemberUserEntity
 *
 * @author maurice.chen
 *
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MemberUserService extends BasicService<MemberUserDao, MemberUserEntity> {

    private final AuthorizationService authorizationService;

    public MemberUserService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * 更新会员用户密码
     *
     * @param userId 会员用户 id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void updateMemberUserPassword(Integer userId, String oldPassword, String newPassword) {
        MemberUserEntity memberUser = get(userId);

        PasswordEncoder passwordEncoder = authorizationService
                .getUserDetailsService(ResourceSourceEnum.USER_CENTER)
                .getPasswordEncoder();

        if (YesOrNo.Yes.equals(memberUser.getInitialization().getRandomPassword())) {

            if (!passwordEncoder.matches(oldPassword, memberUser.getPassword())) {
                throw new ServiceException("旧密码不正确");
            }

        } else {
            memberUser.getInitialization().setRandomPassword(YesOrNo.No);
        }

        lambdaUpdate()
                .set(SystemUserEntity::getPassword, passwordEncoder.encode(newPassword))
                .set(MemberUserEntity::getInitialization, memberUser.getInitialization())
                .eq(SystemUserEntity::getId, userId)
                .update();

        authorizationService.expireSystemUserSession(memberUser.getUsername());
    }

    /**
     * 更新会员登录账户
     *
     * @param userId 会员用户 id
     * @param newUsername 新登录账户
     */
    public void updateMemberUserUsername(Integer userId, String newUsername) {
        MemberUserEntity memberUser = get(userId);

        if (YesOrNo.No.equals(memberUser.getInitialization().getRandomUsername())) {
            throw new ServiceException("不能多次修改登录账户");
        }

        if (!find(Wrappers.<MemberUserEntity>lambdaQuery().eq(MemberUserEntity::getUsername, newUsername)).isEmpty()) {
            throw new ServiceException("登录账户 [" + newUsername + "] 已存在");
        }

        memberUser.getInitialization().setRandomUsername(YesOrNo.No);

        lambdaUpdate()
                .set(SystemUserEntity::getUsername, newUsername)
                .set(MemberUserEntity::getInitialization, memberUser.getInitialization())
                .eq(SystemUserEntity::getId, memberUser.getId())
                .update();

        authorizationService.expireSystemUserSession(memberUser.getUsername());
    }

    /**
     * 根据唯一识别获取会员用户
     *
     * @param identified 唯一识别
     *
     * @return 会员用户
     */
    public MemberUserEntity getByIdentified(String identified) {
        return lambdaQuery()
                .eq(MemberUserEntity::getUsername, identified)
                .or()
                .eq(MemberUserEntity::getPhone, identified)
                .or().eq(MemberUserEntity::getEmail, identified)
                .one();
    }
}
