
package com.fuyu.basic.support.crypto.algorithm;

import java.security.SecureRandom;

/**
 * 随机数值生成器接口 用于生成下一个字节原而使用
 *
 * @author maurice
 */
public interface RandomNumberGenerator {

    /**
     * 生成下一个字节元
     *
     * @return 字节原
     */
    ByteSource nextBytes();

    /**
     * 生成下一个字节元
     *
     * @param numBytes 字节数
     * @return 字节原
     */
    ByteSource nextBytes(int numBytes);

    /**
     * 获取随机对象
     *
     * @return 随机对象
     */
    SecureRandom getRandom();
}
