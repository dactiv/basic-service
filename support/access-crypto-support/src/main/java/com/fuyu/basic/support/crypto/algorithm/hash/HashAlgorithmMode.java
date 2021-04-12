
package com.fuyu.basic.support.crypto.algorithm.hash;

/**
 * hash 算法模型
 *
 * @author maurice
 */
public enum HashAlgorithmMode {
    /**
     * MD5 hash
     */
    MD5("MD5"),
    /**
     * SHA-1 hash
     */
    SHA1("SHA-1"),
    /**
     * SHA-256 hash
     */
    SHA256("SHA-256"),
    /**
     * SHA-384 hash
     */
    SHA384("SHA-384"),
    /**
     * SHA-512 hash
     */
    SHA512("SHA-512");

    /**
     * hash 算法模型
     *
     * @param name 算法名称
     */
    private HashAlgorithmMode(String name) {
        this.name = name;
    }

    /**
     * 算法名称
     */
    private String name;

    /**
     * 获取算法名称
     *
     * @return 算法名称
     */
    public String getName() {
        return name;
    }
}
