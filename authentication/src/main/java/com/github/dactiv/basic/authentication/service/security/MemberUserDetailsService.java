package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.basic.authentication.dao.entity.Group;
import com.github.dactiv.basic.authentication.dao.entity.MemberUser;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.basic.authentication.service.security.config.AuthenticationProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;

/**
 * 会员用户明细服务实现
 *
 * @author maurice.chen
 */
@Component
@RefreshScope
public class MemberUserDetailsService implements UserDetailsService {

    private static final String DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME = "type";

    private static final String IS_MOBILE_PATTERN_STRING = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";

    public static final String DEFAULT_IS_NEW_MEMBER_KEY_NAME = "isNewMember";

    @Autowired
    private AuthenticationProperties authenticationProperties;


    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CaptchaService captchaService;

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        String type = token.getHttpServletRequest().getParameter(DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME);

        if (StringUtils.isEmpty(type)) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        MemberUser user = userService.getMemberUserByIdentified(token.getPrincipal().toString());

        if (user == null) {

            if (ResourceSource.Mobile.toString().equals(type)) {

                String phone = token.getPrincipal().toString();

                if (!StringUtils.isNumeric(phone)
                        || !Pattern.compile(IS_MOBILE_PATTERN_STRING).matcher(phone).matches()) {
                    throw new BadCredentialsException("手机号码不正确");
                }

                user = new MemberUser();

                user.setPhone(token.getPrincipal().toString());
                user.setPassword(generateRandomPassword());

                int count = authenticationProperties.getRegister().getRandomUsernameCount();
                user.setUsername(RandomStringUtils.randomAlphanumeric(count) + user.getPhone());

                user.setStatus(UserStatus.Enabled.getValue());

            } else if (ResourceSource.AnonymousUser.toString().equals(type)) {

                user = new MemberUser();

                user.setUsername(token.getPrincipal().toString());
                user.setPassword(getPasswordEncoder().encode(token.getCredentials().toString()));

                user.setStatus(UserStatus.Enabled.getValue());

            } else {
                throw new UsernameNotFoundException("用户名或密码错误");
            }

        }

        UserStatus status = NameValueEnumUtils.parse(user.getStatus(), UserStatus.class);

        return new SecurityUserDetails(user.getId(), user.getUsername(), user.getPassword(), status);
    }

    /**
     * 生成随机密码
     *
     * @return 密码
     */
    protected String generateRandomPassword() {
        int count = authenticationProperties.getRegister().getRandomUsernameCount();
        String key = RandomStringUtils.randomAlphanumeric(count) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    @Override
    public boolean matchesPassword(String presentedPassword,
                                   RequestAuthenticationToken token,
                                   SecurityUserDetails userDetails) {

        String type = token.getHttpServletRequest().getParameter(DEFAULT_AUTHENTICATION_TYPE_PARAM_NAME);

        if (!ResourceSource.Mobile.toString().equals(type)) {
            boolean matches = getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());

            if (ResourceSource.AnonymousUser.toString().equals(type) && matches && userDetails.getId() == null) {
                createMemberUser(token, userDetails);
            }

            return matches;

        } else {

            Map<String, Object> params = new LinkedHashMap<>();

            params.put(authenticationProperties.getCaptcha().getTokenParamName(), token.getHttpServletRequest().getParameter(authenticationProperties.getCaptcha().getTokenParamName()));
            params.put(authenticationProperties.getCaptcha().getCaptchaParamName(), presentedPassword);
            params.put(authenticationProperties.getMobile().getUsernameParamName(), token.getHttpServletRequest().getParameter(CaptchaAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY));

            RestResult<Map<String, Object>> result;

            try {
                result = captchaService.verifyCaptcha(params);
            } catch (Exception e) {
                throw new AuthenticationServiceException("调用验证码服务出现异常", e);
            }

            if (result.getStatus() == HttpStatus.OK.value()) {

                token.getHttpServletRequest().setAttribute(DEFAULT_IS_NEW_MEMBER_KEY_NAME, false);

                if (userDetails.getId() == null) {

                    createMemberUser(token, userDetails, token.getPrincipal().toString());

                }

                return true;
            } else {
                throw new BadCredentialsException(result.getMessage());
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
    private MemberUser createMemberUser(RequestAuthenticationToken token, SecurityUserDetails userDetails) {
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
    private MemberUser createMemberUser(RequestAuthenticationToken token, SecurityUserDetails userDetails, String phone) {
        MemberUser user = new MemberUser();

        user.setPhone(phone);
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setStatus(userDetails.getStatus());

        userService.saveMemberUser(
                user,
                Collections.singletonList(authenticationProperties.getRegister().getDefaultGroup())
        );

        userDetails.setId(user.getId());

        token.getHttpServletRequest().setAttribute(DEFAULT_IS_NEW_MEMBER_KEY_NAME, true);

        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {
        Integer userId = Casts.cast(userDetails.getId());

        List<Group> groups = userService
                .getAuthorizationService()
                .getMemberUserGroups(userId);

        userDetails.setRoleAuthorities(
                groups
                        .stream()
                        .map(g -> new RoleAuthority(g.getName(), g.getAuthority()))
                        .collect(Collectors.toList())
        );

        return userDetails.getAuthorities();
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSource.UserCenter.toString());
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public UserService getUserService() {
        return userService;
    }
}
