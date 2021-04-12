
package com.fuyu.basic.support.crypto.algorithm.cipher;

/**
 * Cipher 的填充方案枚举
 *
 * @author maurice
 */
public enum PaddingScheme {

    /**
     * 不填充，当块大小为8位用于块密码流操作时有用(因为一个字节是最原始的块大小，所以没有什么可以填充).
     */
    NONE("NoPadding"),

    /**
     * XML加密语法和处理填充方案
     */
    ISO10126("ISO10126Padding"),

    /**
     * RSA 的 PKSC＃1 标准填充方案
     */
    OAEP("OAEPPadding"),

    /**
     * 使用{@code MD5}消息摘要和{@code MGF1}掩码生成功能的最优非对称加密填充。
     */
    OAEPWithMd5AndMgf1("OAEPWithMD5AndMGF1Padding"),

    /**
     * 使用{@code SHA-1}消息摘要和{@code MGF1}掩码生成功能的最优非对称加密填充。
     */
    OAEPWithSha1AndMgf1("OAEPWithSHA-1AndMGF1Padding"),

    /**
     * 使用{@code SHA-256}消息摘要和{@code MGF1}掩码生成功能的最优非对称加密填充。
     */
    OAEPWithSha256AndMgf1("OAEPWithSHA-256AndMGF1Padding"),

    /**
     * 使用{@code SHA-384}消息摘要和{@code MGF1}掩码生成功能的最优非对称加密填充。
     */
    OAEPWithSha384AndMgf1("OAEPWithSHA-384AndMGF1Padding"),

    /**
     * 使用 {@code SHA-512} 消息摘要和 {@code MGF1} 掩码生成功能的最优非对称加密填充。
     */
    OAEPWithSha512AndMgf1("OAEPWithSHA-512AndMGF1Padding"),

    /**
     * 第一代基于密码的密码学标准 RSA 非对称加密填充方案
     */
    PKCS1("PKCS1Padding"),

    /**
     * 第五代基于密码的密码学标准 RSA 非对称加密填充方案
     */
    PKCS5("PKCS5Padding"),

    /**
     * 第七代基于密码的密码学标准 RSA 非对称加密填充方案
     */
    PKCS7("PKCS7Padding"),

    /**
     * 第八代基于密码的密码学标准 RSA 非对称加密填充方案
     */
    PKCS8("PKCS8Padding"),

    /**
     * SSL 3.0 填充方案
     */
    SSL3("SSL3Padding");

    /**
     * Cipher 的填充方案枚举
     *
     * @param transformationName Cipher 转型名称
     */
    private PaddingScheme(String transformationName) {
        this.transformationName = transformationName;
    }

    /**
     * Cipher 转型名称
     */
    private final String transformationName;

    /**
     * 获取实际的 Cipher 转型名称
     *
     * @return 转型名称
     * @see javax.crypto.Cipher#getInstance(String)
     */
    public String getTransformationName() {
        return this.transformationName;
    }

    /**
     * 通过 Cipher 转型名称获取本枚举
     *
     * @param name Cipher 转型名称
     * @return PaddingScheme
     */
    public static PaddingScheme getPaddingScheme(String name) {
        for (PaddingScheme paddingScheme : PaddingScheme.values()) {
            if (paddingScheme.getTransformationName().equals(name) || paddingScheme.name().equals(name)) {
                return paddingScheme;
            }
        }
        return null;
    }
}
