package com.fuyu.basic.support.security.authentication;

import com.fuyu.basic.support.security.authentication.token.RequestAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证 filter 实现, 用于结合 {@link UserDetailsService} 多用户类型认证的统一入口
 *
 * @author maurice.chen
 */
public class RequestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * 默认的认证类型 header 名称
     */
    public static final String SPRING_SECURITY_FORM_TYPE_HEADER_NAME = "X-AUTHENTICATION-TYPE";
    public static final String SPRING_SECURITY_FORM_TYPE_PARAM_NAME = "authenticationType";

    public final static String SPRING_SECURITY_ANONYMOUS_USER_TYPE = "AnonymousUser";
    /**
     * 认证类型 header 名称
     */
    private String typeHeaderName = SPRING_SECURITY_FORM_TYPE_HEADER_NAME;

    private String typeParamName = SPRING_SECURITY_FORM_TYPE_PARAM_NAME;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        Authentication token = createToken(request, response);

        return getAuthenticationManager().authenticate(token);
    }

    /**
     * 创建当前用户认证 token
     *
     * @param request  http servlet request
     * @param response http servlet response
     * @return 当前用户认证 token
     * @throws AuthenticationException 认证异常
     */
    protected Authentication createToken(HttpServletRequest request,
                                         HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        String type = obtainType(request);

        if (StringUtils.isEmpty(type)) {
            throw new AuthenticationServiceException("授权类型不正确");
        }

        if (type.equals(SPRING_SECURITY_ANONYMOUS_USER_TYPE)) {
            return new UsernamePasswordAuthenticationToken(username, password);
        } else {
            return new RequestAuthenticationToken(request, response, username, password, type);
        }

    }

    /**
     * 获取类型
     *
     * @param request http servlet request
     * @return 类型
     */
    protected String obtainType(HttpServletRequest request) {
        return request.getHeader(typeHeaderName);
    }

    /**
     * 获取认证类型 header 名称
     *
     * @return 认证类型 header 名称
     */
    public String getTypeHeaderName() {
        return typeHeaderName;
    }

    /**
     * 设置认证类型 header 名称
     *
     * @param typeHeaderName 认证类型 header 名称
     */
    public void setTypeHeaderName(String typeHeaderName) {
        this.typeHeaderName = typeHeaderName;
    }

    /**
     * 获取认证类型参数名称
     *
     * @return 参数名称
     */
    public String getTypeParamName() {
        return typeParamName;
    }

    /**
     * 设置认证类型参数名称
     *
     * @param typeParamName 参数名称
     */
    public void setTypeParamName(String typeParamName) {
        this.typeParamName = typeParamName;
    }
}
