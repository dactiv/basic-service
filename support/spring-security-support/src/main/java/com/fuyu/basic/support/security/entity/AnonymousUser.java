package com.fuyu.basic.support.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

/**
 * 匿名用户实现
 *
 * @author maurice
 */
public class AnonymousUser extends User {

    private static final long serialVersionUID = -1107846840344957633L;

    /**
     * 默认的匿名用户登录账户
     */
    public final static String DEFAULT_ANONYMOUS_USERNAME = "anonymousUser";

    /**
     * 默认的匿名用户角色名称
     */
    private final static String DEFAULT_ANONYMOUS_ROLE = "ROLE_ANONYMOUS";

    /**
     * 临时密码
     */
    private final String tempPassword;

    public AnonymousUser(String username,
                         String password,
                         String tempPassword,
                         Collection<? extends GrantedAuthority> authorities) {

        super(username, password, authorities);
        this.tempPassword = tempPassword;

    }

    public AnonymousUser(String password, String tempPassword) {
        this(
                DEFAULT_ANONYMOUS_USERNAME,
                password,
                tempPassword,
                Collections.singleton(new SimpleGrantedAuthority(DEFAULT_ANONYMOUS_ROLE))
        );
    }

    public AnonymousUser(String password,
                         String tempPassword,
                         Collection<? extends GrantedAuthority> authorities) {
        this(DEFAULT_ANONYMOUS_USERNAME, password, tempPassword, authorities);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return super.getPassword();
    }

    /**
     * 获取真实密码
     *
     * @return 真实密码
     */
    public String getTempPassword() {
        return tempPassword;
    }


}
