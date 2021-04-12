

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 加密服务
 *
 * @author maurice
 */
public interface EncryptService {

    /**
     * 加密内容
     *
     * @param plainText 需要加密的内容
     * @param key       密钥
     * @return 加密后的内容字节原
     * @throws CryptoException 加密出错时抛出
     */
    ByteSource encrypt(byte[] plainText, byte[] key) throws CryptoException;

    /**
     * 加密内容
     *
     * @param in  需要加密的内容输入流
     * @param out 加密后需要返回明文的输出流
     * @param key 密钥
     * @throws CryptoException 加密出错时抛出
     */
    void encrypt(InputStream in, OutputStream out, byte[] key) throws CryptoException;
}
