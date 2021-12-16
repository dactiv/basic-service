package com.github.dactiv.basic.authentication.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.domain.entity.GroupEntity;
import com.github.dactiv.basic.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.basic.authentication.domain.meta.MemberUserInitializationMeta;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.GroupService;
import com.github.dactiv.basic.authentication.service.MemberUserService;
import com.github.dactiv.basic.commons.authentication.IdRoleAuthority;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.commons.feign.captcha.CaptchaFeignClient;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 会员用户明细服务实现
 *
 * @author maurice.chen
 */
@Component
@RefreshScope
public class MemberUserDetailsService implements UserDetailsService {

    private static final String DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME = "type";

    public static final String IS_MOBILE_PATTERN_STRING = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";

    public static final String DEFAULT_IS_NEW_MEMBER_KEY_NAME = "isNewMember";

    @Getter
    private final ApplicationConfig applicationConfig;

    @Getter
    private final AuthorizationService authorizationService;

    @Getter
    private final MemberUserService memberUserService;

    private final GroupService groupService;

    private final PasswordEncoder passwordEncoder;

    private final CaptchaFeignClient captchaFeignClient;

    public MemberUserDetailsService(ApplicationConfig applicationConfig,
                                    AuthorizationService userService,
                                    GroupService groupService,
                                    MemberUserService memberUserService,
                                    PasswordEncoder passwordEncoder,
                                    CaptchaFeignClient captchaFeignClient) {
        this.applicationConfig = applicationConfig;
        this.authorizationService = userService;
        this.groupService = groupService;
        this.memberUserService = memberUserService;
        this.passwordEncoder = passwordEncoder;
        this.captchaFeignClient = captchaFeignClient;
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        String type = token.getHttpServletRequest().getParameter(DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME);

        if (StringUtils.isBlank(type)) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        MemberUserEntity user = memberUserService.getByIdentified(token.getPrincipal().toString());

        if (Objects.isNull(user)) {

            user = new MemberUserEntity();

            user.setPhone(token.getPrincipal().toString());
            user.setPassword(generateRandomPassword());
            user.setStatus(UserStatus.Enabled);
            user.setInitialization(new MemberUserInitializationMeta());

            if (ResourceSourceEnum.MOBILE.toString().equals(type)) {

                String phone = token.getPrincipal().toString();

                if (!StringUtils.isNumeric(phone)
                        || !Pattern.compile(IS_MOBILE_PATTERN_STRING).matcher(phone).matches()) {
                    throw new BadCredentialsException("手机号码不正确");
                }

                int count = applicationConfig.getRegister().getRandomUsernameCount();
                user.setUsername(RandomStringUtils.randomAlphanumeric(count) + user.getPhone());

            } else if (!ResourceSourceEnum.ANONYMOUS_USER.toString().equals(type)) {
                throw new UsernameNotFoundException("用户名或密码错误");
            }

        }
        return new SecurityUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getStatus());
    }

    /**
     * 生成随机密码
     *
     * @return 密码
     */
    protected String generateRandomPassword() {
        int count = applicationConfig.getRegister().getRandomUsernameCount();
        String key = RandomStringUtils.randomAlphanumeric(count) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    @Override
    public boolean matchesPassword(String presentedPassword,
                                   RequestAuthenticationToken token,
                                   SecurityUserDetails userDetails) {

        String type = token.getHttpServletRequest().getParameter(DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME);

        if (!ResourceSourceEnum.MOBILE.toString().equals(type)) {
            boolean matches = getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());

            if (ResourceSourceEnum.ANONYMOUS_USER.toString().equals(type) && matches && userDetails.getId() == null) {
                createMemberUser(token, userDetails);
            }

            return matches;

        } else {

            Map<String, Object> params = new LinkedHashMap<>();

            String username = token.getHttpServletRequest().getParameter(
                    CaptchaAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY
            );

            String tokenValue = token.getHttpServletRequest().getParameter(
                    applicationConfig.getCaptcha().getTokenParamName()
            );

            params.put(applicationConfig.getCaptcha().getTokenParamName(), tokenValue);
            params.put(applicationConfig.getCaptcha().getCaptchaParamName(), presentedPassword);
            params.put(applicationConfig.getMobile().getUsernameParamName(), username);

            try {

                RestResult<Map<String, Object>> result = captchaFeignClient.verifyCaptcha(params);

                if (result.getStatus() == HttpStatus.OK.value()) {

                    token.getHttpServletRequest().setAttribute(DEFAULT_IS_NEW_MEMBER_KEY_NAME, false);

                    if (userDetails.getId() == null) {

                        createMemberUser(token, userDetails, token.getPrincipal().toString());

                    }

                    return true;
                } else {
                    throw new BadCredentialsException(result.getMessage());
                }

            } catch (Exception e) {
                throw new AuthenticationServiceException("调用验证码服务出现异常", e);
            }


        }
    }

    /**
     * 创建一个基础会员用户
     *
     * @param token       认证 token
     * @param userDetails 用户信息
     *
     * @return 新的会员用户
     */
    private MemberUserEntity createMemberUser(RequestAuthenticationToken token, SecurityUserDetails userDetails) {
        return createMemberUser(token, userDetails, null);
    }

    /**
     * 创建一个基础会员用户
     *
     * @param token       认证 token
     * @param userDetails 用户信息
     * @param phone       手机号码
     *
     * @return 新的会员用户
     */
    private MemberUserEntity createMemberUser(RequestAuthenticationToken token, SecurityUserDetails userDetails, String phone) {
        MemberUserEntity user = new MemberUserEntity();

        user.setPhone(phone);
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setStatus(userDetails.getStatus());

        GroupEntity group = groupService.get(applicationConfig.getRegister().getDefaultGroup());
        IdRoleAuthority roleAuthority = new IdRoleAuthority(group.getId(), group.getName(), group.getAuthority());
        List<IdRoleAuthority> roleAuthorities = Collections.singletonList(roleAuthority);
        user.setGroupsInfo(Casts.convertValue(roleAuthorities, new TypeReference<>() {}));

        memberUserService.save(user);
        userDetails.setId(user.getId());
        token.getHttpServletRequest().setAttribute(DEFAULT_IS_NEW_MEMBER_KEY_NAME, true);

        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {
        Integer userId = Casts.cast(userDetails.getId());
        MemberUserEntity memberUser = memberUserService.get(userId);
        authorizationService.setSystemUserAuthorities(memberUser, userDetails);
        return userDetails.getAuthorities();
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSourceEnum.USER_CENTER.toString());
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }
}
