package com.fuyu.basic.authentication.service.security;

import com.fuyu.basic.authentication.dao.entity.ConsoleUser;
import com.fuyu.basic.authentication.dao.entity.Group;
import com.fuyu.basic.authentication.dao.entity.Resource;
import com.fuyu.basic.authentication.service.UserService;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.enumerate.NameValueEnumUtils;
import com.fuyu.basic.support.security.authentication.UserDetailsService;
import com.fuyu.basic.support.security.authentication.token.RequestAuthenticationToken;
import com.fuyu.basic.support.security.entity.ResourceAuthority;
import com.fuyu.basic.support.security.entity.RoleAuthority;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
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

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("usernameEq", token.getPrincipal());

        ConsoleUser user = userService.getConsoleUserByFilter(filter);

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        UserStatus status = NameValueEnumUtils.parse(user.getStatus(), UserStatus.class);

        return new SecurityUserDetails(user.getId(), user.getUsername(), user.getPassword(), status);
    }

    @Override
    public Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {

        Map<String, Object> filter = new LinkedHashMap<>();

        filter.put(
                "sourceContains",
                Arrays.asList(
                        userDetails.getType(),
                        ResourceSource.All.toString(),
                        ResourceSource.System.toString()
                )
        );

        Integer userId = Casts.cast(userDetails.getId());

        List<Resource> resources = userService
                .getAuthorizationService()
                .getConsolePrincipalResources(userId, filter);

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
