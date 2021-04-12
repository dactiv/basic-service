
package com.fuyu.basic.support.crypto.algorithm.hash;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;

/**
 * hash 请求对象
 *
 * @author maurice
 */
public class HashRequest {

    /**
     * 需要 hash 的值
     */
    private ByteSource byteSource;

    /**
     * 盐值
     */
    private ByteSource salt;

    /**
     * 迭代次数
     */
    private int iterations;

    /**
     * 算法
     */
    private String algorithmName;

    /**
     * hash 请求对象
     */
    public HashRequest() {

    }

    /**
     * hash 请求对象
     *
     * @param byteSource 需要 hash 的值
     */
    public HashRequest(ByteSource byteSource) {
        this.byteSource = byteSource;
    }

    /**
     * hash 请求对象
     *
     * @param byteSource    需要 hash 的值
     * @param algorithmName 算法
     */
    public HashRequest(ByteSource byteSource, String algorithmName) {
        this.byteSource = byteSource;
        this.algorithmName = algorithmName;
    }

    /**
     * hash 请求对象
     *
     * @param byteSource 需要 hash 的值
     * @param salt       盐值
     */
    public HashRequest(ByteSource byteSource, ByteSource salt) {
        this.byteSource = byteSource;
        this.salt = salt;
    }

    /**
     * hash 请求对象
     *
     * @param byteSource 需要 hash 的值
     * @param salt       盐值
     * @param iterations hash 迭代次数
     */
    public HashRequest(ByteSource byteSource, ByteSource salt, int iterations) {
        this.byteSource = byteSource;
        this.salt = salt;
        this.iterations = iterations;
    }

    /**
     * hash 请求对象
     *
     * @param byteSource    需要 hash 的值
     * @param salt          盐值
     * @param algorithmName 算法
     */
    public HashRequest(ByteSource byteSource, ByteSource salt, String algorithmName) {
        this.byteSource = byteSource;
        this.salt = salt;
        this.algorithmName = algorithmName;
    }

    /**
     * hash 请求对象
     *
     * @param byteSource    需要 hash 的值
     * @param salt          盐值
     * @param iterations    hash 迭代次数
     * @param algorithmName 算法
     */
    public HashRequest(ByteSource byteSource, ByteSource salt, int iterations, String algorithmName) {
        this.byteSource = byteSource;
        this.salt = salt;
        this.iterations = iterations;
        this.algorithmName = algorithmName;
    }

    /**
     * 设置需要 hash 的值
     *
     * @param byteSource 值
     */
    public void setByteSource(ByteSource byteSource) {
        this.byteSource = byteSource;
    }

    /**
     * 设置盐值
     *
     * @param salt 盐值
     */
    public void setSalt(ByteSource salt) {
        this.salt = salt;
    }

    /**
     * 设置 hash 迭代次数
     *
     * @param iterations 迭代次数
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * 设置算法
     *
     * @param algorithmName hash 算法
     * @see HashAlgorithmMode
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    /**
     * 获取需要 hash 的值
     *
     * @return 值
     */
    public ByteSource getSource() {
        return byteSource;
    }

    /**
     * 获取盐值
     *
     * @return 盐值
     */
    public ByteSource getSalt() {
        return salt;
    }

    /**
     * 获取 hash 迭代次数
     *
     * @return 迭代次数
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * 获取 hash 算法
     *
     * @return hash 算法
     */
    public String getAlgorithmName() {
        return algorithmName;
    }
}
