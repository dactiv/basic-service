package com.github.dactiv.basic.authentication.security;

import com.github.dactiv.basic.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.ConsoleUserService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 系统用户明细服务实现
 *
 * @author maurice.chen
 */
@Component
public class ConsoleUserDetailsService implements UserDetailsService {

    private final AuthorizationService authorizationService;

    private final ConsoleUserService consoleUserService;

    private final PasswordEncoder passwordEncoder;

    public ConsoleUserDetailsService(AuthorizationService authorizationService,
                                     ConsoleUserService consoleUserService,
                                     PasswordEncoder passwordEncoder) {
        this.authorizationService = authorizationService;
        this.consoleUserService = consoleUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        ConsoleUserEntity user = consoleUserService.getByIdentity(token.getPrincipal().toString());

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        SecurityUserDetails userDetails = new SecurityUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus()
        );

        authorizationService.setSystemUserAuthorities(user, userDetails);

        return userDetails;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSourceEnum.CONSOLE.toString());
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
