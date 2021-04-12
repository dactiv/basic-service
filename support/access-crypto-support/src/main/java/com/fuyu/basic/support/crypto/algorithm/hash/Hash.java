
package com.fuyu.basic.support.crypto.algorithm.hash;

import com.fuyu.basic.support.crypto.algorithm.AbstractByteSource;
import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.CodecUtils;
import com.fuyu.basic.support.crypto.algorithm.SimpleByteSource;
import com.fuyu.basic.support.crypto.algorithm.exception.CodecException;
import com.fuyu.basic.support.crypto.algorithm.exception.UnknownAlgorithmException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hash 类，该类对对象类型 byte[]、{@link ByteSource} char[]、String、File、InputStream 支持 hash 计算，
 * 算法使用{@link MessageDigest} 所支持的 hash 算法，
 *
 * <pre>
 *     例子:
 *
 *     String s = "123456"
 *     Hash hash = new Hash("MD5", s);
 *     // 得到 hash 后的 16 进制
 *     hash.getHex();
 *     // 得到 hash 后的 base64
 *     hash.getBase64();
 * </pre>
 *
 * @author maurice
 */
public class Hash extends AbstractByteSource implements Serializable {

    /**
     * 默认的迭代次数
     */
    private static final int DEFAULT_ITERATIONS = 1;

    /**
     * 算法名称
     */
    private final String algorithmName;

    /**
     * 字节数组
     */
    private byte[] bytes;

    /**
     * 盐值
     */
    private ByteSource salt;

    /**
     * 需要 hash 的次数
     */
    private int iterations;

    /**
     * 简单的 hash 实现
     *
     * @param algorithmName 算法名称
     * @param bytes         字节数组
     */
    public Hash(String algorithmName, byte[] bytes) {
        this.algorithmName = algorithmName;
        this.bytes = bytes;
    }

    /**
     * 简单的 hash 实现
     *
     * @param algorithmName 算法名称
     * @param source        需要 hash 的原
     */
    public Hash(String algorithmName, Object source) {
        this(algorithmName, source, null, DEFAULT_ITERATIONS);
    }

    /**
     * 简单的 hash 实现
     *
     * @param algorithmName  算法名称
     * @param source         需要 hash 的原
     * @param salt           盐值
     * @param hashIterations hash 迭代次数
     */
    public Hash(String algorithmName, Object source, Object salt, int hashIterations) {
        if (algorithmName == null || "".equals(algorithmName.trim())) {
            throw new IllegalArgumentException("算法参数不能空");
        }
        this.algorithmName = algorithmName;
        this.iterations = Math.max(DEFAULT_ITERATIONS, hashIterations);
        ByteSource saltBytes = null;
        if (salt != null) {
            saltBytes = new SimpleByteSource(CodecUtils.toBytes(salt));
            this.salt = saltBytes;
        }
        ByteSource sourceBytes = new SimpleByteSource(CodecUtils.toBytes(source));
        this.bytes = hash(sourceBytes, saltBytes, hashIterations);
    }

    /**
     * hash 得到一个字节数组
     *
     * @param source         需要 hash 的原
     * @param salt           盐值
     * @param hashIterations hash 迭代次数
     * @return hash 后的字节数组
     * @throws CodecException            当编码失败时抛出
     * @throws UnknownAlgorithmException 出现未知的 hash 算法时抛出
     */
    private byte[] hash(ByteSource source, ByteSource salt, int hashIterations) throws CodecException, UnknownAlgorithmException {
        //是否使用盐，如果使用获取盐的字节数组
        byte[] saltBytes = salt != null ? salt.obtainBytes() : null;
        // 获取加密后的 hash 值
        return hash(source.obtainBytes(), saltBytes, hashIterations);
    }

    /**
     * hash 得到一个字节数组
     *
     * @param bytes          字节数组
     * @param salt           盐值
     * @param hashIterations hash 迭代次数
     * @return hash 后的字节数组
     * @throws UnknownAlgorithmException 出现未知的 hash 算法时抛出
     */
    protected byte[] hash(byte[] bytes, byte[] salt, int hashIterations) throws UnknownAlgorithmException {
        MessageDigest digest = getDigest(getAlgorithmName());

        // 如果使用盐，加入盐
        if (salt != null) {
            digest.reset();
            digest.update(salt);
        }
        // 得到 hash 值
        byte[] hashed = digest.digest(bytes);
        // 迭代次数
        int iterations = hashIterations - 1;
        // 如果需要循环 hash iterations 次，循环完成后在返回
        for (int i = 0; i < iterations; i++) {
            digest.reset();
            hashed = digest.digest(hashed);
        }

        return hashed;
    }

    /**
     * 获取原生的 hash 算法实现
     *
     * @param algorithmName 算法
     * @return {@link MessageDigest}
     * @throws UnknownAlgorithmException 出现未知的 hash 算法时抛出
     */
    private MessageDigest getDigest(String algorithmName) throws UnknownAlgorithmException {
        try {
            return MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            String msg = "当前环境不能支持 " + algorithmName + " 算法";
            throw new UnknownAlgorithmException(msg, e);
        }
    }

    /**
     * 获取算法名称，该名称要求 {@link java.security.MessageDigest} 所支持的算法名称
     *
     * @return 算法名称
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * 获取盐值，该值用于在 hash 时候通过该盐再次计算混淆计算
     *
     * @return 字节原，如果没有为 null
     */
    public ByteSource getSalt() {
        return salt;
    }

    /**
     * 获取 hash 迭代值，该值用于说明需要使用 {@link Hash#getAlgorithmName()} 的算法 hash 多少次
     *
     * @return 迭代值
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * 设置盐值，该值用于在 hash 时候通过该盐再次计算混淆计算
     *
     * @param salt 盐值
     */
    public void setSalt(ByteSource salt) {
        this.salt = salt;
    }

    /**
     * 设置 hash 迭代值，该值用于说明需要使用 {@link Hash#getAlgorithmName()} 的算法 hash 多少次
     *
     * @param iterations 算法 hash 多少次
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public byte[] obtainBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Hash) {
            Hash other = (Hash) o;
            return MessageDigest.isEqual(obtainBytes(), other.obtainBytes());
        }
        return false;
    }
}
