

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.exception.UnknownAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * 对称加密服务
 *
 * @author maurice
 */
public class SymmetricCipherService extends AbstractBlockCipherService {

    /**
     * 对称加密服务
     *
     * @param algorithmName 算法名称
     */
    public SymmetricCipherService(String algorithmName) {
        super(algorithmName);

    }

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    @Override
    public Key generateKey() {
        return generateKey(getKeySize());
    }

    /**
     * 生成密钥
     *
     * @param keySize 密钥大小
     * @return 密钥
     */
    public Key generateKey(int keySize) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(getAlgorithmName());
        } catch (NoSuchAlgorithmException e) {
            String msg = "对称加密不支持 " + getAlgorithmName() + " 算法";
            throw new UnknownAlgorithmException(msg, e);
        }
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    @Override
    protected Key getCipherSecretKey(int mode, byte[] key, String algorithmName) {
        return new SecretKeySpec(key, getAlgorithmName());
    }
}
