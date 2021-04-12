

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * 暗号信息类，用于在创建加解密暗号时，通过该类去明确当前暗号的参数信息
 *
 * @author maurice
 */
public class CipherInfo {

    /**
     * 暗号模型(加密或解密)
     */
    private int mode;

    /**
     * 算法名称
     */
    private String algorithmName;

    /**
     * 暗号
     */
    private Cipher cipher;

    /**
     * 密钥
     */
    private Key key;

    /**
     * 向量参数规格
     */
    private IvParameterSpec ivParameterSpec;

    /**
     * 安全随机数
     */
    private SecureRandom secureRandom;

    /**
     * 暗号信息类
     *
     * @param mode          暗号模型
     * @param algorithmName 算法名称
     * @param key           密钥
     */
    public CipherInfo(int mode, String algorithmName, Key key) {
        this(mode, algorithmName, key, null, null);
    }

    /**
     * 暗号信息类
     *
     * @param mode            暗号模型
     * @param algorithmName   算法名称
     * @param key             密钥
     * @param ivParameterSpec 向量参数规格
     */
    public CipherInfo(int mode, String algorithmName, Key key, IvParameterSpec ivParameterSpec) {
        this(mode, algorithmName, key, ivParameterSpec, null);
    }

    /**
     * 暗号信息类
     *
     * @param mode          暗号模型
     * @param algorithmName 算法名称
     * @param key           密钥
     * @param secureRandom  安全随机数
     */
    public CipherInfo(int mode, String algorithmName, Key key, SecureRandom secureRandom) {
        this(mode, algorithmName, key, null, secureRandom);
    }

    /**
     * 暗号信息类
     *
     * @param mode            暗号模型
     * @param algorithmName   算法名称
     * @param key             密钥
     * @param ivParameterSpec 向量参数规格
     * @param secureRandom    安全随机数
     */
    public CipherInfo(int mode, String algorithmName, Key key, IvParameterSpec ivParameterSpec, SecureRandom secureRandom) {

        this.key = key;
        this.ivParameterSpec = ivParameterSpec;
        this.secureRandom = secureRandom;
        this.mode = mode;
        this.algorithmName = algorithmName;

        try {

            this.cipher = Cipher.getInstance(algorithmName);

            if (secureRandom != null) {

                if (ivParameterSpec != null) {
                    cipher.init(mode, key, ivParameterSpec, secureRandom);
                } else {
                    cipher.init(mode, key, secureRandom);
                }

            } else {
                if (ivParameterSpec != null) {
                    cipher.init(mode, key, ivParameterSpec);
                } else {
                    cipher.init(mode, key);
                }
            }
        } catch (Exception e) {
            String msg = "无法初始化 Cipher";
            throw new CryptoException(msg, e);
        }
    }

    /**
     * 获取加解密暗号
     *
     * @return 加解密暗号
     */
    public Cipher getCipher() {
        return cipher;
    }

    /**
     * 设置加解密暗号
     *
     * @param cipher 加解密暗号
     */
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * 获取暗号密钥
     *
     * @return 暗号密钥
     */
    public Key getKey() {
        return key;
    }

    /**
     * 设置暗号密钥
     *
     * @param key 暗号密钥
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * 获取向量参数规格
     *
     * @return 向量参数规格
     */
    public IvParameterSpec getIvParameterSpec() {
        return ivParameterSpec;
    }

    /**
     * 设置向量参数规格
     *
     * @param ivParameterSpec 向量参数规格
     */
    public void setIvParameterSpec(IvParameterSpec ivParameterSpec) {
        this.ivParameterSpec = ivParameterSpec;
    }

    /**
     * 获取安全随机数
     *
     * @return 安全随机数
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    /**
     * 设置安全随机数
     *
     * @param secureRandom 安全随机数
     */
    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * 获取暗号模型
     *
     * @return 暗号模型
     */
    public int getMode() {
        return mode;
    }

    /**
     * 设置暗号模型
     *
     * @param mode 暗号模型
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 获取算法名称
     *
     * @return 算法名称
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * 设置算法名称
     *
     * @param algorithmName 算法名称
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }
}
