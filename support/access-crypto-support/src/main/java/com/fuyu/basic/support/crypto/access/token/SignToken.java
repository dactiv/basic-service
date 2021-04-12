
package com.fuyu.basic.support.crypto.access.token;

import com.fuyu.basic.support.crypto.access.AccessToken;
import com.fuyu.basic.support.crypto.algorithm.ByteSource;

/**
 * 带签名信息的 token 实现，用于非对称加密数据后，客户端需要校验签名使用
 *
 * @author maurice
 */
public class SignToken extends SimpleToken {

    private static final long serialVersionUID = -7966065869382142057L;
    /**
     * 签名信息
     */
    private ByteSource sign;

    /**
     * 带签名信息的客户端实现
     *
     * @param sign 签名信息
     */
    public SignToken(ByteSource sign) {
        this.sign = sign;
    }

    /**
     * 带签名信息的客户端实现
     *
     * @param token 客户端信息
     * @param sign  签名信息
     */
    public SignToken(AccessToken token, ByteSource sign) {
        super(token.getType(), token.getToken(), token.getName(), token.getKey());
        this.sign = sign;
    }

    /**
     * 获取签名信息
     *
     * @return 签名信息
     */
    public ByteSource getSign() {
        return sign;
    }

    /**
     * 设置签名信息
     *
     * @param sign 签名信息
     */
    public void setSign(ByteSource sign) {
        this.sign = sign;
    }
}
