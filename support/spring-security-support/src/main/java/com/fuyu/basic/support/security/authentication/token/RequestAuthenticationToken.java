package com.fuyu.basic.support.security.authentication.token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求认证 token
 *
 * @author maurice
 */
public class RequestAuthenticationToken extends PrincipalAuthenticationToken {

    private static final long serialVersionUID = 8070060147431763553L;

    /**
     * http servlet request
     */
    private final HttpServletRequest httpServletRequest;

    /**
     * http servlet response
     */
    private final HttpServletResponse httpServletResponse;

    /**
     * 请求认证 token
     *
     * @param httpServletRequest  http servlet request
     * @param httpServletResponse http servlet response
     * @param principal           当前用户
     * @param credentials         凭证
     * @param type                认证类型
     */
    public RequestAuthenticationToken(HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse,
                                      Object principal,
                                      Object credentials,
                                      String type) {
        super(principal, credentials, type);
        this.httpServletResponse = httpServletResponse;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 获取 http servlet request
     *
     * @return http servlet request
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * 获取 http servlet response
     *
     * @return http servlet response
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }
}
