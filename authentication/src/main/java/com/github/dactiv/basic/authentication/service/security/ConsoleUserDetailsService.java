package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.basic.authentication.dao.entity.ConsoleUser;
import com.github.dactiv.basic.authentication.dao.entity.Group;
import com.github.dactiv.basic.authentication.dao.entity.Resource;
import com.github.dactiv.basic.authentication.service.UserService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.ResourceAuthority;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        return new SecurityUserDetails(user.getId(), user.getUsername(), user.getPassword(), status);
    }

    @Override
    public Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {

        List<String> sourceContains = Arrays.asList(
                userDetails.getType(),
                ResourceSource.All.toString(),
                ResourceSource.System.toString()
        );

        Integer userId = Casts.cast(userDetails.getId());

        List<Resource> resources = userService
                .getAuthorizationService()
                .getConsolePrincipalResources(userId, sourceContains, null);

        userDetails.setResourceAuthorities(
                resources
                        .stream()
                        .filter(r -> StringUtils.contains(r.getAuthority(), ResourceAuthority.DEFAULT_RESOURCE_PREFIX))
                        .flatMap(r -> {

                            String[] permissions = StringUtils.substringsBetween(
                                    r.getAuthority(),
                                    ResourceAuthority.DEFAULT_RESOURCE_PREFIX,
                                    ResourceAuthority.DEFAULT_RESOURCE_SUFFIX
                            );

                            return Arrays.stream(permissions).map(p -> {
                                String v = ResourceAuthority.DEFAULT_RESOURCE_PREFIX
                                        + p
                                        + ResourceAuthority.DEFAULT_RESOURCE_SUFFIX;

                                return new ResourceAuthority(v, r.getName(), r.getValue());
                            });

                        })
                        .collect(Collectors.toList())
        );


        List<Group> groups = userService
                .getAuthorizationService()
                .getConsoleUserGroups(userId);

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
        return Collections.singletonList(ResourceSource.Console.toString());
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
