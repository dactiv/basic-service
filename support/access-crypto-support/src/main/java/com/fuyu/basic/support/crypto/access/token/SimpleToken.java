package com.fuyu.basic.support.crypto.access.token;

import com.fuyu.basic.support.crypto.access.AccessToken;
import com.fuyu.basic.support.crypto.algorithm.ByteSource;

import java.util.UUID;

/**
 * 简单的 token 实现
 *
 * @author maurice
 */
public class SimpleToken implements AccessToken {

    private static final long serialVersionUID = 427463584890877727L;

    /**
     * 类型
     */
    private String type;

    /**
     * token 值
     */
    private String token;

    /**
     * 名称
     */
    private String name;

    /**
     * 密钥
     */
    private ByteSource key;

    /**
     * 简单的 token
     *
     * @param type  类型
     * @param token token 值
     * @param name  名称
     * @param key   密钥
     */
    public SimpleToken(String type, String token, String name, ByteSource key) {
        this.type = type;
        this.token = token;
        this.name = name;
        this.key = key;
    }

    /**
     * 简单的 token
     */
    public SimpleToken() {
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getToken() {
        return token;
    }

    /**
     * 设置 token 值
     *
     * @param token token 值
     */
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ByteSource getKey() {
        return key;
    }

    /**
     * 设置密钥
     *
     * @param key 密钥
     */
    public void setKey(ByteSource key) {
        this.key = key;
    }

    /**
     * 创建一个简单的 token
     *
     * @param type 类型
     * @param name 名称
     * @param key  密钥
     * @return 简单的 token
     */
    public static SimpleToken build(String type, String name, ByteSource key) {
        return new SimpleToken(type, UUID.randomUUID().toString(), name, key);
    }

    /**
     * 创建一个生成密钥类型的 token
     *
     * @param name 名称
     * @param key  密钥
     * @return 简单的 token
     */
    public static SimpleToken generate(String name, ByteSource key) {
        return build(GENERATE_KEY_TYPE, name, key);
    }

    /**
     * 创建一个请求解密类型的 token
     *
     * @param name 名称
     * @param key  密钥
     * @return 简单的 token
     */
    public static SimpleToken requestDecrypt(String name, ByteSource key) {
        return build(REQUEST_DECRYPT_TYPE, name, key);
    }

    /**
     * 创建一个响应加密的 token
     *
     * @param name 名称
     * @param key  密钥
     * @return 简单的 token
     */
    public static SimpleToken responseEncrypt(String name, ByteSource key) {
        return build(RESPONSE_ENCRYPT_TYPE, name, key);
    }

    @Override
    public String toString() {
        return "SimpleToken{" +
                "type='" + type + '\'' +
                ", token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", key=" + key.getBase64() +
                '}';
    }
}
