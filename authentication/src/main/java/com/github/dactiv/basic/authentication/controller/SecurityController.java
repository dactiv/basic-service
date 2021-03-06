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
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.audit.Auditable;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
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
 * ???????????????
 *
 * @author maurice.chen
 */
@RefreshScope
@RestController
public class SecurityController {

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final MobileUserDetailsService mobileUserDetailsService;

    private final MemberUserService memberUserService;

    private final ConsoleUserService consoleUserService;

    public SecurityController(JsonLogoutSuccessHandler jsonLogoutSuccessHandler,
                              MobileUserDetailsService mobileUserDetailsService,
                              ConsoleUserService consoleUserService,
                              MemberUserService memberUserService) {
        this.jsonLogoutSuccessHandler = jsonLogoutSuccessHandler;
        this.mobileUserDetailsService = mobileUserDetailsService;
        this.consoleUserService = consoleUserService;
        this.memberUserService = memberUserService;
    }

    /**
     * ???????????????
     *
     * @param request http servlet request
     *
     * @return rest ?????????
     */
    @GetMapping("prepare")
    public RestResult<Map<String, Object>> prepare(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * ????????????
     *
     * @return ?????????????????????
     */
    @GetMapping("login")
    public RestResult<Map<String, Object>> login(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param securityContext ???????????????
     *
     * @return ????????????
     */
    @GetMapping("getPrincipal")
    @PreAuthorize("isAuthenticated()")
    public SecurityUserDetails getPrincipal(@CurrentSecurityContext SecurityContext securityContext) {
        return Casts.cast(securityContext.getAuthentication().getDetails());
    }

    /**
     * ??????????????????
     *
     * @param type ????????????
     * @param ids  ?????? id
     *
     * @return ????????????
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
            throw new SystemException("?????????????????? [" + type + "] ???????????????");
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param deviceIdentified ????????????
     * @param username         ????????????
     * @param password         ????????????
     *
     * @return ??????????????????????????????
     */
    @PreAuthorize("isAuthenticated()")
    @Auditable(type = "??????????????????????????????")
    @PostMapping("validMobileUserDetails")
    public MobileUserDetails validMobileUserDetails(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                                    @RequestParam String username,
                                                    @RequestParam String password) {
        RBucket<MobileUserDetails> bucket = mobileUserDetailsService.getMobileUserDetailsBucket(username);
        MobileUserDetails mobileUserDetails = bucket.get();

        if (mobileUserDetails == null) {
            throw new ServiceException("?????????[" + username + "]?????????????????????");
        }

        if (!mobileUserDetails.getDeviceIdentified().equals(deviceIdentified)) {
            throw new ServiceException("?????? ID ?????????");
        }

        String rawPassword = DigestUtils.md5DigestAsHex((password + username + deviceIdentified).getBytes())
                + mobileUserDetails.getDevice().toString();

        if (!mobileUserDetailsService.getPasswordEncoder().matches(rawPassword, mobileUserDetails.getPassword())) {
            throw new ServiceException("?????????????????????");
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
