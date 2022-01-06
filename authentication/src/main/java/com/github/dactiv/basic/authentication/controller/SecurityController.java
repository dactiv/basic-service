package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.security.MobileUserDetailsService;
import com.github.dactiv.basic.authentication.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.basic.authentication.service.ConsoleUserService;
import com.github.dactiv.basic.authentication.service.MemberUserService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.audit.Auditable;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.spring.security.audit.StringIdEntity;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import org.redisson.api.RBucket;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final AuditEventRepository auditEventRepository;

    private final MobileUserDetailsService mobileUserDetailsService;

    private final MemberUserService memberUserService;

    private final ConsoleUserService consoleUserService;

    public SecurityController(JsonLogoutSuccessHandler jsonLogoutSuccessHandler,
                              AuditEventRepository auditEventRepository,
                              MobileUserDetailsService mobileUserDetailsService,
                              ConsoleUserService consoleUserService,
                              MemberUserService memberUserService) {
        this.jsonLogoutSuccessHandler = jsonLogoutSuccessHandler;
        this.auditEventRepository = auditEventRepository;
        this.mobileUserDetailsService = mobileUserDetailsService;
        this.consoleUserService = consoleUserService;
        this.memberUserService = memberUserService;
    }

    /**
     * 获取用户审计数据
     *
     * @param pageRequest 分页请求
     * @param principal   用户登陆账户
     * @param after       数据发生时间
     * @param type        审计类型
     *
     * @return 审计事件
     */
    @PostMapping("audit")
    @PreAuthorize("hasAuthority('perms[audit:page]')")
    @Plugin(
            name = "操作审计",
            id = "audit",
            icon = "icon-audit",
            parent = "system",
            type = ResourceType.Menu,
            sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
    )
    public Page<AuditEvent> audit(PageRequest pageRequest,
                                  @RequestParam(required = false) String principal,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date after,
                                  @RequestParam(required = false) String type) {

        if (!PluginAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {
            throw new ServiceException("当前审计不支持分页查询");
        }

        PluginAuditEventRepository pageAuditEventRepository = Casts.cast(auditEventRepository);

        Instant instant = Objects.nonNull(after) ? after.toInstant() : null;

        return pageAuditEventRepository.findPage(pageRequest, principal, instant, type);
    }

    /**
     * 获取用户审计数据
     *
     * @param id    主键 id
     * @param after 数据发生时间
     *
     * @return 用户审计数据
     */
    @GetMapping("getAudit")
    @PreAuthorize("hasAuthority('perms[audit:get]')")
    @Plugin(name = "获取操作审计", parent = "audit", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public AuditEvent getAudit(@RequestParam(required = false) String id,
                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date after) {

        if (!PluginAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {
            throw new ServiceException("当前审计不支持分页查询");
        }

        PluginAuditEventRepository pluginAuditEventRepository = Casts.cast(auditEventRepository);

        StringIdEntity stringIdEntity = new StringIdEntity();

        stringIdEntity.setId(id);
        stringIdEntity.setTimestamp(after.toInstant());

        return pluginAuditEventRepository.get(stringIdEntity);
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
     * 获取用户信息
     *
     * @param type 来源类型
     * @param ids  用户 id
     *
     * @return 用户信息
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "getPrincipalProfile", method = {RequestMethod.GET, RequestMethod.POST})
    public List<? extends NumberIdEntity<Integer>> getPrincipalProfile(@RequestParam String type,
                                                                       @RequestParam List<Integer> ids) {
        if (ResourceSourceEnum.CONSOLE.toString().equals(type)) {
            return consoleUserService.get(ids);
        } else if (ResourceSourceEnum.USER_CENTER.toString().equals(type)) {
            return memberUserService.get(ids);
        } else {
            throw new SystemException("找不到类型为 [" + type + "] 的用户信息");
        }
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
    @PreAuthorize("isAuthenticated()")
    @Auditable(type = "验证移动用户明细信息")
    @PostMapping("validMobileUserDetails")
    public MobileUserDetails validMobileUserDetails(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                                    @RequestParam String username,
                                                    @RequestParam String password) {
        RBucket<MobileUserDetails> bucket = mobileUserDetailsService.getMobileUserDetailsBucket(username);
        MobileUserDetails mobileUserDetails = bucket.get();

        if (mobileUserDetails == null) {
            throw new ServiceException("找不到[" + username + "]的移动用户明细");
        }

        if (!mobileUserDetails.getDeviceIdentified().equals(deviceIdentified)) {
            throw new ServiceException("设备 ID 不匹配");
        }

        String rawPassword = DigestUtils.md5DigestAsHex((password + username + deviceIdentified).getBytes())
                + mobileUserDetails.getDevice().toString();

        if (!mobileUserDetailsService.getPasswordEncoder().matches(rawPassword, mobileUserDetails.getPassword())) {
            throw new ServiceException("用户名密码错误");
        }

        UsernamePasswordAuthenticationToken usernamePasswordToken = new UsernamePasswordAuthenticationToken(
                mobileUserDetails.getUsername(),
                mobileUserDetails.getPassword()
        );

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                usernamePasswordToken,
                mobileUserDetails.getType()
        );

        token.setDetails(mobileUserDetails);

        mobileUserDetailsService.onSuccessAuthentication(token);

        return mobileUserDetails;
    }

}
