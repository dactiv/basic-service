
package com.fuyu.basic.support.crypto.algorithm.cipher;

/**
 * 密码分组模式枚举
 *
 * @author maurice
 */
public enum OperationMode {

    /**
     * Cipher-block Chaining JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    CBC,

    /**
     * Cipher Feedback 模式，JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    CFB,

    /**
     * Counter Mode 模式, JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    CTR,

    /**
     * Electronic Codebook 模式，JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    ECB,

    /**
     * No Mode ，JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    NONE,

    /**
     * Output Feedback 模式，JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    OFB,

    /**
     * Propagating Cipher Block Chaining 模式，JDK 的标准分组密码模式，该模式被 JDK 支持所有运行环境
     */
    PCBC
}
