package com.fuyu.basic.support.crypto.access;

import java.io.Serializable;

/**
 * 加解密算法模型
 *
 * @author maurice
 */
public class CryptoAlgorithm implements Serializable {

    private static final long serialVersionUID = -3207978889614838003L;
    /**
     * 算法
     */
    private String algorithm;

    /**
     * 密码分组模式
     */
    private String mode;

    /**
     * 块大小
     */
    private int blockSize;

    /**
     * 填充方案
     */
    private String paddingScheme;

    /**
     * 以流加密解密的分组模型
     */
    private String streamingMode;

    /**
     * 以流加密解密的块大小
     */
    private int streamingBlockSize;

    /**
     * 以流加密解密的填充方案名称
     */
    private String streamingPaddingScheme;

    /**
     * 密钥大小
     */
    private int keySize;

    /**
     * 初始化向量大小
     */
    private int initializationVectorSize;

    /**
     * 加解密算法
     */
    public CryptoAlgorithm() {
    }

    /**
     * 获取算法
     *
     * @return 算法
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 设置算法
     *
     * @param algorithm 算法
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 获取密码分组模式
     *
     * @return 密码分组模式
     */
    public String getMode() {
        return mode;
    }

    /**
     * 设置密码分组模式
     *
     * @param mode 密码分组模式
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 获取块大小
     *
     * @return 块大小
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * 设置块大小
     *
     * @param blockSize 块大小
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * 获取填充方案
     *
     * @return 填充方案
     */
    public String getPaddingScheme() {
        return paddingScheme;
    }

    /**
     * 设置填充方案
     *
     * @param paddingScheme 填充方案
     */
    public void setPaddingScheme(String paddingScheme) {
        this.paddingScheme = paddingScheme;
    }

    /**
     * 获取以流加密解密的分组模型
     *
     * @return 分组模型
     */
    public String getStreamingMode() {
        return streamingMode;
    }

    /**
     * 设置以流加密解密的分组模型
     *
     * @param streamingMode 分组模型
     */
    public void setStreamingMode(String streamingMode) {
        this.streamingMode = streamingMode;
    }

    /**
     * 获取以流加密解密的块大小
     *
     * @return 块大小
     */
    public int getStreamingBlockSize() {
        return streamingBlockSize;
    }

    /**
     * 设置以流加密解密的块大小
     *
     * @param streamingBlockSize 块大小
     */
    public void setStreamingBlockSize(int streamingBlockSize) {
        this.streamingBlockSize = streamingBlockSize;
    }

    /**
     * 获取以流加密解密的填充方案名称
     *
     * @return 填充方案名称
     */
    public String getStreamingPaddingScheme() {
        return streamingPaddingScheme;
    }

    /**
     * 设置以流加密解密的填充方案名称
     *
     * @param streamingPaddingScheme 填充方案名称
     */
    public void setStreamingPaddingScheme(String streamingPaddingScheme) {
        this.streamingPaddingScheme = streamingPaddingScheme;
    }

    /**
     * 获取密钥大小
     *
     * @return 密钥大小
     */
    public int getKeySize() {
        return keySize;
    }

    /**
     * 设置密钥大小
     *
     * @param keySize 密钥大小
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    /**
     * 获取初始化向量大小
     *
     * @return 初始化向量大小
     */
    public int getInitializationVectorSize() {
        return initializationVectorSize;
    }

    /**
     * 设置初始化向量大小
     *
     * @param initializationVectorSize 初始化向量大小
     */
    public void setInitializationVectorSize(int initializationVectorSize) {
        this.initializationVectorSize = initializationVectorSize;
    }
}
