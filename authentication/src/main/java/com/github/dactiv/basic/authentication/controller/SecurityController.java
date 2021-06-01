package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.dao.entity.MemberUser;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.basic.authentication.service.security.MobileUserDetailsService;
import com.github.dactiv.basic.authentication.service.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.security.audit.Auditable;
import com.github.dactiv.framework.spring.security.audit.PageAuditEventRepository;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 授权控制器
 *
 * @author maurice.chen
 */
@RefreshScope
@RestController
public class SecurityController {

    @Autowired
    private UserService userService;

    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private MobileUserDetailsService mobileUserDetailsService;

    /**
     * 获取用户审计数据
     *
     * @param pageable  分页请求
     * @param principal 用户登陆账户
     * @param after     数据发生时间
     * @param type      审计类型
     *
     * @return 审计事件
     */
    @PostMapping("audit")
    @Plugin(name = "审计管理", id = "audit", parent = "system", type = ResourceType.Menu, sources = "Console")
    public Page<AuditEvent> audit(@PageableDefault Pageable pageable,
                                  @RequestParam(required = false) String principal,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date after,
                                  @RequestParam(required = false) String type) {

        if (!PageAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {
            throw new ServiceException("当前审计不支持分页查询");
        }

        PageAuditEventRepository pageAuditEventRepository = Casts.cast(auditEventRepository);

        Instant instant = Objects.nonNull(after) ? after.toInstant() : null;

        return pageAuditEventRepository.findPage(pageable, principal, instant, type);
    }

    /**
     * 登录预处理
     *
     * @param request http servlet request
     *
     * @return rest 结果集
     */
    @GetMapping("prepare")
    public RestResult<Map<String, Object>> prepare(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 用户登陆
     *
     * @return 未授权访问结果
     */
    @GetMapping("login")
    public RestResult<Map<String, Object>> login(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 登陆成功后跳转的连接，直接获取当前用户
     *
     * @param securityContext 安全上下文
     *
     * @return 当前用户
     */
    @GetMapping("getPrincipal")
    @PreAuthorize("isAuthenticated()")
    public SecurityUserDetails getPrincipal(@CurrentSecurityContext SecurityContext securityContext) {
        return Casts.cast(securityContext.getAuthentication().getDetails());
    }

    /**
     * 验证移动用户明细信息
     *
     * @param deviceIdentified 唯一识别
     * @param username         登录账户
     * @param password         登录密码
     *
     * @return 移动端的用户明细实现
     */
    @PreAuthorize("hasRole('ORDINARY')")
    @Auditable(type = "验证移动用户明细信息")
    @PostMapping("validMobileUserDetails")
    public MobileUserDetails validMobileUserDetails(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                                    @RequestParam String username,
                                                    @RequestParam String password) {

        MobileUserDetails mobileUserDetails = mobileUserDetailsService.getMobileUserDetailsBucket(username).get();

        if (mobileUserDetails == null) {
            throw new ServiceException("找不到[" + username + "]的移动用户明细");
        }

        if (!mobileUserDetails.getDeviceIdentified().equals(deviceIdentified)) {
            throw new ServiceException("设备 ID 不匹配");
        }

        String rawPassword = DigestUtils.md5DigestAsHex((password + username + deviceIdentified).getBytes())
                + mobileUserDetails.getDevice().toString()
                + mobileUserDetails.getDevice().getDevicePlatform().name();

        if (!mobileUserDetailsService.getPasswordEncoder().matches(rawPassword, mobileUserDetails.getPassword())) {
            throw new ServiceException("用户名密码错误");
        }

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                mobileUserDetails.getUsername(),
                mobileUserDetails.getPassword(),
                mobileUserDetails.getType()
        );

        token.setDetails(mobileUserDetails);

        mobileUserDetailsService.onSuccessAuthentication(token);

        return mobileUserDetails;
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
    @PostMapping("updateMemberUserStatus")
    public RestResult<?> updateMemberUserStatus(@RequestParam Integer id, @RequestParam String status) {
        MemberUser memberUser = userService.getMemberUser(id);

        memberUser.setStatus(UserStatus.valueOf(status).getValue());

        userService.updateMemberUser(memberUser);

        return RestResult.ofSuccess("修改成功");
    }

}
