package com.github.dactiv.basic.authentication.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.entity.MemberUser;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
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
        sources = "Console"
)
public class MemberUserConsole {

    @Autowired
    private UserService userService;

    @Autowired
    private MybatisPlusQueryGenerator<MemberUser> queryGenerator;

    /**
     * 查找会员用户分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[member_user:page]')")
    @Plugin(name = "查询分页", sources = "Console")
    public Page<MemberUser> page(PageRequest pageRequest, HttpServletRequest request) {
        return userService.findMemberUserPage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 查找会员用户信息
     * @param request http 请求
     * @return 会员用户信息集合
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "查询分页", sources = "System")
    public List<MemberUser> find(HttpServletRequest request) {
        return userService.findMemberUsers(queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取会员用户实体
     *
     * @param id 主键值
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("isAuthenticated()")
    public MemberUser get(@RequestParam Integer id) {
        return userService.getMemberUser(id);
    }

    /**
     * 更新会员用户登陆密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     * @return 消息结果集
     */
    @PostMapping("updatePassword")
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "修改密码", sources = {"Console", "UserCenter", "Mobile"}, audit = true)
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam(required = false) String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer userId = Casts.cast(userDetails.getId());
        userService.updateMemberUserPassword(userId, oldPassword, newPassword);

        return RestResult.of("修改成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     * @return true 是，否则 false
     */
    @GetMapping("isUsernameUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isUsernameUnique(@RequestParam String username) {
        return userService.findMemberUsers(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getUsername, username)).isEmpty();
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isEmailUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isEmailUnique(@RequestParam String email) {
        return userService.findMemberUsers(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getEmail, email)).isEmpty();
    }

    /**
     * 判断手机号码是否唯一
     *
     * @param phone 手机号码
     * @return true 是，否则 false
     */
    @GetMapping("isPhoneUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isPhoneUnique(@RequestParam String phone) {
        return userService.findMemberUsers(Wrappers.<MemberUser>lambdaQuery().eq(MemberUser::getPhone, phone)).isEmpty();
    }

}
