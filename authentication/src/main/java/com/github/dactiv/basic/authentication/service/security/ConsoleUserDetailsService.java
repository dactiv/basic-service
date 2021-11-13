package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceService;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.ResourceAuthority;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 系统用户明细服务实现
 *
 * @author maurice.chen
 */
@Component
public class ConsoleUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        ConsoleUser user = userService.getConsoleUserByUsername(token.getPrincipal().toString());

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        UserStatus status = NameValueEnumUtils.parse(user.getStatus(), UserStatus.class);

        SecurityUserDetails userDetails = new SecurityUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                status
        );

        userService.getAuthorizationService().setSystemUserAuthorities(user, userDetails);

        return userDetails;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSource.Console.toString());
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
