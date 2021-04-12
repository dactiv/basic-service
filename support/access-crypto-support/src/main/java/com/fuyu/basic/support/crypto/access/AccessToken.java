package com.fuyu.basic.support.crypto.access;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;

import java.io.Serializable;

/**
 * 访问 token
 *
 * @author maurice
 */
public interface AccessToken extends Serializable {

    /**
     * 生成 key 类型
     */
    String GENERATE_KEY_TYPE = "GENERATE_KEY";

    /**
     * 公共密钥名称
     */
    String PUBLIC_TOKEN_KEY_NAME = "PUBLIC_KEY";

    /**
     * 私有密钥名称
     */
    String ACCESS_TOKEN_KEY_NAME = "ACCESS_KEY";

    /**
     * response 加密类型
     */
    String RESPONSE_ENCRYPT_TYPE = "RESPONSE_ENCRYPT";

    /**
     * request 解密类型
     */
    String REQUEST_DECRYPT_TYPE = "REQUEST_DECRYPT";

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getType();

    /**
     * 获取名称
     *
     * @return 名称值
     */
    String getName();

    /**
     * 获取 token
     *
     * @return token 值
     */
    String getToken();

    /**
     * 获取密钥
     *
     * @return 密钥
     */
    ByteSource getKey();
}
