package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.basic.authentication.service.MemberUserService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 会员用户管理
 *
 * @author maurice
 */
@RestController
@RequestMapping("member/user")
@Plugin(
        name = "会员用户管理",
        id = "member_user",
        parent = "system",
        icon = "icon-user-groups",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class MemberUserConsole {

    private final MemberUserService memberUserService;

    private final MybatisPlusQueryGenerator<MemberUserEntity> queryGenerator;

    public MemberUserConsole(MemberUserService memberUserService,
                             MybatisPlusQueryGenerator<MemberUserEntity> queryGenerator) {
        this.memberUserService = memberUserService;
        this.queryGenerator = queryGenerator;
    }

    /**
     * 查找会员用户分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[member_user:page]')")
    @Plugin(name = "查询分页", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<MemberUserEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return memberUserService.findPage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 查找会员用户信息
     *
     * @param request http 请求
     *
     * @return 会员用户信息集合
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "查询分页", sources = ResourceSourceEnum.SYSTEM_SOURCE_VALUE)
    public List<MemberUserEntity> find(HttpServletRequest request) {
        return memberUserService.find(queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取会员用户实体
     *
     * @param id 主键值
     *
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("isAuthenticated()")
    public MemberUserEntity get(@RequestParam Integer id) {
        return memberUserService.get(id);
    }

    /**
     * 更新会员用户登陆密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     *
     * @return 消息结果集
     */
    @PostMapping("updatePassword")
    @PreAuthorize("hasRole('ORDINARY') and isFullyAuthenticated()")
    @Plugin(
            name = "修改密码",
            sources = {
                    ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
                    ResourceSourceEnum.USER_CENTER_SOURCE_VALUE,
                    ResourceSourceEnum.MOBILE_SOURCE_VALUE
            },
            audit = true
    )
    @Idempotent(key = "idempotent:authentication:member:update-password:[#securityContext.authentication.details.id]")
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam(required = false) String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer userId = Casts.cast(userDetails.getId());
        memberUserService.updateMemberUserPassword(userId, oldPassword, newPassword);

        return RestResult.of("修改成功");
    }


    /**
     * 更新会员用户登陆账户
     *
     * @param securityContext 安全上下文
     * @param newUsername     新登录账户
     *
     * @return 消息结果集
     */
    @PostMapping("updateUsername")
    @PreAuthorize("hasRole('ORDINARY') and isFullyAuthenticated()")
    @Plugin(
            name = "修改登录账户", 
            sources = {ResourceSourceEnum.USER_CENTER_SOURCE_VALUE, ResourceSourceEnum.MOBILE_SOURCE_VALUE},
            audit = true
    )
    @Idempotent(key = "idempotent:authentication:member:update-username:[#securityContext.authentication.details.id]")
    public RestResult<?> updateUsername(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String newUsername) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        memberUserService.updateMemberUserUsername(userId, newUsername);

        return RestResult.of("修改成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     *
     * @return true 是，否则 false
     */
    @GetMapping("isUsernameUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isUsernameUnique(@RequestParam String username) {
        return memberUserService.lambdaQuery().eq(MemberUserEntity::getUsername, username).exists();
    }

    /**
     * 更新会员用户状态
     *
     * @param id     用户 id
     * @param status 状态值
     *
     * @return 消息结果集
     */
    @PreAuthorize("hasRole('BASIC')")
    @PostMapping("updateStatus")
    public RestResult<?> updateMemberUserStatus(@RequestParam Integer id, @RequestParam Integer status) {
        MemberUserEntity memberUser = memberUserService.get(id);
        MemberUserEntity update = memberUser.ofIdData();

        update.setStatus(ValueEnumUtils.parse(status, UserStatus.class));

        memberUserService.save(update);

        return RestResult.of("修改成功");
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     *
     * @return true 是，否则 false
     */
    @GetMapping("isEmailUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isEmailUnique(@RequestParam String email) {
        return memberUserService.lambdaQuery().eq(MemberUserEntity::getEmail, email).exists();
    }

    /**
     * 判断手机号码是否唯一
     *
     * @param phone 手机号码
     *
     * @return true 是，否则 false
     */
    @GetMapping("isPhoneUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isPhoneUnique(@RequestParam String phone) {
        return memberUserService.lambdaQuery().eq(MemberUserEntity::getPhone, phone).exists();
    }

}
