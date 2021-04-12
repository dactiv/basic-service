package com.fuyu.basic.authentication.controller;

import com.fuyu.basic.authentication.dao.entity.ConsoleUser;
import com.fuyu.basic.authentication.service.UserService;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.page.Page;
import com.fuyu.basic.commons.page.PageRequest;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        source = ResourceSource.Console
)
public class ConsoleUserController {

    /**
     * 账户管理服务
     */
    @Autowired
    private UserService userService;

    /**
     * 查找系统用户分页信息
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[console_user:page]')")
    @Plugin(name = "查询分页", source = ResourceSource.Console)
    public Page<ConsoleUser> page(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return userService.findConsoleUserPage(pageRequest, filter);
    }

    /**
     * 获取系统用户实体
     *
     * @param id 主键值
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[console_user:get]')")
    @Plugin(name = "获取信息", source = ResourceSource.Console)
    public ConsoleUser get(@RequestParam Integer id) {
        return userService.getConsoleUser(id);
    }

    /**
     * 保存系统用户实体
     *
     * @param entity      系统用户实体
     * @param groupIds    用户组 ID 集合
     * @param resourceIds 用户组资源 ID 集合
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[console_user:save]')")
    @Plugin(name = "保存", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@Valid ConsoleUser entity,
                                                       @RequestParam(required = false) List<Integer> groupIds,
                                                       @RequestParam(required = false) List<Integer> resourceIds) {

        userService.saveConsoleUser(entity, groupIds, resourceIds);

        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除系统用户
     *
     * @param ids 系统用户主键 ID 集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[console_user:delete]')")
    @Plugin(name = "删除", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {

        userService.deleteConsoleUsers(ids);

        return RestResult.build("删除" + ids.size() + "条记录成功");
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
    @Plugin(name = "修改密码", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                               @RequestParam String oldPassword,
                                               @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Integer userId = Casts.cast(userDetails.getId());
        userService.updateConsoleUserPassword(userId, oldPassword, newPassword);

        return RestResult.build("修改密码成功");
    }

    /**
     * 更新会员用户登陆账户
     *
     * @param securityContext 安全上下文
     * @param newUsername     新登录账户
     * @return 消息结果集
     */
    @PostMapping("updateUsername")
    @PreAuthorize("hasRole('ORDINARY')")
    @Plugin(name = "修改登录账户", source = {ResourceSource.UserCenter, ResourceSource.Mobile}, audit = true)
    public RestResult.Result<?> updateUsername(@CurrentSecurityContext SecurityContext securityContext,
                                               @RequestParam String newUsername) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Integer userId = Casts.cast(userDetails.getId());
        userService.updateMemberUserUsername(userId, newUsername);

        return RestResult.build("修改成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     * @return true 是，否则 false
     */
    @GetMapping("isUsernameUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断登录账户是否唯一", source = ResourceSource.Console)
    public boolean isUsernameUnique(@RequestParam String username) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("usernameEq", username);
        return userService.findConsoleUsers(filter).isEmpty();
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isEmailUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断邮件是否唯一", source = ResourceSource.Console)
    public boolean isEmailUnique(@RequestParam String email) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("emailEq", email);
        return userService.findConsoleUsers(filter).isEmpty();
    }

}
