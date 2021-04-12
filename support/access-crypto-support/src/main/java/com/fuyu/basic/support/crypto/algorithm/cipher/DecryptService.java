

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 解密服务
 *
 * @author maurice
 */
public interface DecryptService {

    /**
     * 解密内容
     *
     * @param cipherText 需要解密的内容
     * @param key        密钥
     * @return 解密后的内容字节元素
     * @throws CryptoException 解密出错时抛出
     */
    ByteSource decrypt(byte[] cipherText, byte[] key) throws CryptoException;

    /**
     * 解密内容
     *
     * @param in  需要解密的内容输入流
     * @param out 解密后需要返回明文的输出流
     * @param key 密钥
     * @throws CryptoException 解密出错时抛出
     */
    void decrypt(InputStream in, OutputStream out, byte[] key) throws CryptoException;
}
