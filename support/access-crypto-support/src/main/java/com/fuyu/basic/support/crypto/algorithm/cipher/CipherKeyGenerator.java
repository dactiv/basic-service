
package com.fuyu.basic.support.crypto.algorithm.cipher;

import java.security.Key;

/**
 * 密钥生成器
 *
 * @author maurice
 */
public interface CipherKeyGenerator {

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    Key generateKey();
}
