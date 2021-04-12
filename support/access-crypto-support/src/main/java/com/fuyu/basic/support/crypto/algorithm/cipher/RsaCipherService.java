

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * RSA 非对称加密实现
 *
 * @author maurice
 */
public class RsaCipherService extends AbstractAsymmetricCipherService {

    private KeyFactory keyFactory;

    /**
     * RSA 非对称加密实现
     */
    public RsaCipherService() {
        this(DEFAULT_KEY_SIZE);
    }

    public RsaCipherService(int keySize) {
        super("RSA");

        setKeySize(keySize);

        setMode(OperationMode.ECB);
        setPaddingScheme(PaddingScheme.PKCS1);

        setStreamingMode(OperationMode.ECB);
        setStreamingPaddingScheme(PaddingScheme.PKCS1);
    }

    @Override
    protected PrivateKey getPrivateKey(byte[] key) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            return getKeyFactory().generatePrivate(keySpec);
        } catch (Exception e) {
            String msg = "解析私有密钥错误:" + Arrays.toString(key);
            throw new CryptoException(msg, e);
        }
    }

    @Override
    protected PublicKey getPublicKey(byte[] key) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            return getKeyFactory().generatePublic(keySpec);
        } catch (Exception e) {
            String msg = "解析公共密钥错误:" + Arrays.toString(key);
            throw new CryptoException(msg, e);
        }
    }

    public KeyFactory getKeyFactory() {
        try {
            if (keyFactory == null) {
                keyFactory = KeyFactory.getInstance(getAlgorithmName());
            }
            return keyFactory;
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }
}
