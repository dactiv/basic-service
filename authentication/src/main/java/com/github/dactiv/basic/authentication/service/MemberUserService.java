package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.dao.MemberUserDao;
import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.entity.MemberUser;

import com.github.dactiv.basic.authentication.entity.SystemUser;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_member_user 的业务逻辑
 *
 * <p>Table: tb_member_user - 会员用户表</p>
 *
 * @see MemberUser
 *
 * @author maurice.chen
 *
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MemberUserService extends BasicService<MemberUserDao, MemberUser> {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * 更新会员用户密码
     *
     * @param userId 用户主键 id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void updateMemberUserPassword(Integer userId, String oldPassword, String newPassword) {
        MemberUser memberUser = get(userId);

        PasswordEncoder passwordEncoder = authorizationService
                .getUserDetailsService(ResourceSource.UserCenter)
                .getPasswordEncoder();

        if (YesOrNo.No.equals(memberUser.getInitialization().getModifyPassword())) {

            if (!passwordEncoder.matches(oldPassword, memberUser.getPassword())) {
                throw new ServiceException("旧密码不正确");
            }

        } else {
            memberUser.getInitialization().setModifyPassword(YesOrNo.Yes);
        }

        lambdaUpdate()
                .set(SystemUser::getPassword, passwordEncoder.encode(newPassword))
                .set(MemberUser::getInitialization, memberUser.getInitialization())
                .eq(SystemUser::getId, userId)
                .update();

        authorizationService.expireSystemUserSession(memberUser.getUsername());
    }

    public void updateMemberUserUsername(Integer userId, String newUsername) {
        MemberUser memberUser = get(userId);

        if (YesOrNo.Yes.equals(memberUser.getInitialization().getModifyUsername())) {
            throw new ServiceException("不能多次修改登录账户");
        }

        if (!find(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getUsername, newUsername)).isEmpty()) {
            throw new ServiceException("登录账户 [" + newUsername + "] 已存在");
        }

        memberUser.getInitialization().setModifyUsername(YesOrNo.Yes);

        lambdaUpdate()
                .set(SystemUser::getUsername, newUsername)
                .set(MemberUser::getInitialization, memberUser.getInitialization())
                .eq(SystemUser::getId, memberUser.getId())
                .update();

        authorizationService.expireSystemUserSession(memberUser.getUsername());
    }
}
