package com.github.dactiv.basic.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.dactiv.basic.authentication.dao.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 系统用户控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("console/user")
@Plugin(
        name = "用户管理",
        id = "console_user",
        parent = "system",
        type = ResourceType.Menu,
        sources = "Console"
)
public class ConsoleUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MybatisPlusQueryGenerator<ConsoleUser> queryGenerator;

    /**
     * 查找系统用户分页信息
     *
     * @param pageable 分页请求
     * @param request  http 请求
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[console_user:page]')")
    @Plugin(name = "查询分页", sources = "Console")
    public Page<ConsoleUser> page(@PageableDefault Pageable pageable, HttpServletRequest request) {
        return userService.findConsoleUserPage(pageable, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取系统用户实体
     *
     * @param id 主键值
     *
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[console_user:get]')")
    @Plugin(name = "获取信息", sources = "Console")
    public ConsoleUser get(@RequestParam Integer id) {
        return userService.getConsoleUser(id);
    }

    /**
     * 保存系统用户实体
     *
     * @param entity      系统用户实体
     * @param groupIds    用户组 ID 集合
     * @param resourceIds 用户组资源 ID 集合
     *
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[console_user:save]')")
    @Plugin(name = "保存", sources = "Console", audit = true)
    public RestResult<Integer> save(@Valid ConsoleUser entity,
                                    @RequestParam(required = false) List<Integer> groupIds,
                                    @RequestParam(required = false) List<Integer> resourceIds) {

        userService.saveConsoleUser(entity, groupIds, resourceIds);

        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除系统用户
     *
     * @param ids 系统用户主键 ID 集合
     *
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[console_user:delete]')")
    @Plugin(name = "删除", sources = "Console", audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {

        userService.deleteConsoleUsers(ids);

        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 更新系统用户登陆密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     */
    @PostMapping("updatePassword")
    @PreAuthorize("hasAuthority('perms[console_user:updatePassword]')")
    @Plugin(name = "修改密码", sources = "Console", audit = true)
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer userId = Casts.cast(userDetails.getId());
        userService.updateConsoleUserPassword(userId, oldPassword, newPassword);

        return RestResult.of("修改密码成功");
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
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "修改登录账户", sources = {"UserCenter", "Mobile"}, audit = true)
    public RestResult<?> updateUsername(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String newUsername) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        userService.updateMemberUserUsername(userId, newUsername);

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
    @Plugin(name = "判断登录账户是否唯一", sources = "Console")
    public boolean isUsernameUnique(@RequestParam String username) {
        return userService.getConsoleUserByUsername(username) == null;
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
    @Plugin(name = "判断邮件是否唯一", sources = "Console")
    public boolean isEmailUnique(@RequestParam String email) {
        return userService.findConsoleUsers(new LambdaQueryWrapper<ConsoleUser>().eq(ConsoleUser::getEmail, email)).isEmpty();
    }

}
