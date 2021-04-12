

package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.RandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SecureRandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SimpleByteSource;
import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;
import com.fuyu.basic.support.crypto.algorithm.exception.UnknownAlgorithmException;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;

/**
 * 非对称加密服务抽象实现
 *
 * @author maurice
 */
public abstract class AbstractAsymmetricCipherService extends AbstractBlockCipherService {

    /**
     * 默认数据签名使用的算法
     */
    public static final String DEFAULT_SIGNATURE_ALGORITHM_NAME = "SHA1withRSA";

    /**
     * 默认密钥大小
     */
    public static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * 默认加解密分段块大小
     */
    public static final int DEFAULT_CRYPT_BLOCK_SIZE = 256;

    /**
     * 默认加密时候需要舍去的位数值
     */
    public static final int DEFAULT_ENCRYPT_ROUNDING_DIGIT = 11;

    /**
     * 分段块大小倍数
     */
    public static final int DEFAULT_BLOCK_SIZE_MULTIPLE = 8;

    /**
     * 数据签名使用的算法
     */
    private String signatureAlgorithmName = DEFAULT_SIGNATURE_ALGORITHM_NAME;

    /**
     * 非对称加密服务
     *
     * @param algorithmName 算法名称
     */
    public AbstractAsymmetricCipherService(String algorithmName) {
        super(algorithmName);
        setKeySize(DEFAULT_KEY_SIZE);
    }

    /**
     * 数据签名
     *
     * @param plainText 明文内容
     * @param key       签名 key
     * @return 签名的字节原
     */
    public ByteSource sign(byte[] plainText, byte[] key) {
        Signature signature = newSignatureInstance(getSignatureAlgorithmName());
        return sign(signature, plainText, key);
    }

    /**
     * 数据签名
     *
     * @param signature 签名的算法实现
     * @param plainText 明文内容
     * @param key       签名 key
     * @return 签名的字节原
     */
    public ByteSource sign(Signature signature, byte[] plainText, byte[] key) {
        initSignSignature(signature, key);

        try {
            signature.update(plainText);
            return new SimpleByteSource(signature.sign());
        } catch (SignatureException e) {
            String msg = "无法签名:" + Arrays.toString(plainText);
            throw new CryptoException(msg, e);
        }
    }

    /**
     * 验证签名
     *
     * @param cipherText 密文内容
     * @param key        验证 key
     * @param sign       签名内容
     * @return true 验证成功，否则 false
     */
    public boolean verify(byte[] cipherText, byte[] key, byte[] sign) {
        Signature signature = newSignatureInstance(getSignatureAlgorithmName());
        initVerifySignature(signature, key);
        return verify(signature, cipherText, key, sign);
    }

    /**
     * 验证签名
     *
     * @param signature  签名的算法实现
     * @param cipherText 密文内容
     * @param key        验证 key
     * @param sign       签名内容
     * @return true 验证成功，否则 false
     */
    public boolean verify(Signature signature, byte[] cipherText, byte[] key, byte[] sign) {
        initVerifySignature(signature, key);
        try {
            signature.update(cipherText);
            return signature.verify(sign);
        } catch (SignatureException e) {
            String msg = "无法验证:" + Arrays.toString(cipherText);
            throw new CryptoException(msg, e);
        }
    }

    /**
     * 初始化签名验证
     *
     * @param signature 签名
     * @param key       验证 key
     */
    private void initVerifySignature(Signature signature, byte[] key) {
        try {
            signature.initVerify(getPublicKey(key));
        } catch (InvalidKeyException e) {
            String msg = "无法初始化 Signature 验证, key 为:" + Arrays.toString(key);
            throw new CryptoException(msg, e);
        }
    }

    /**
     * 初始化签名
     *
     * @param signature 签名
     * @param key       签名 key
     */
    private void initSignSignature(Signature signature, byte[] key) {
        RandomNumberGenerator rng = getRandomNumberGenerator();
        try {
            if (rng != null && SecureRandomNumberGenerator.class.isAssignableFrom(rng.getClass())) {
                SecureRandom secureRandom = (SecureRandom) rng.getRandom();
                signature.initSign(getPrivateKey(key), secureRandom);
            } else {
                signature.initSign(getPrivateKey(key));
            }
        } catch (InvalidKeyException e) {
            String msg = "无法初始化 Signature 签名, key 为:" + Arrays.toString(key);
            throw new CryptoException(msg, e);
        }

    }

    /**
     * 创建一个新的签名
     *
     * @param algorithmName 算法名称
     * @return 签名
     */
    public Signature newSignatureInstance(String algorithmName) {
        try {
            return Signature.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            String msg = "无法执行 " + Signature.class.getName() + ".getInstance( \"" + algorithmName + "\" ). " +
                    algorithmName + " 在当前环境中不支持 ";
            throw new UnknownAlgorithmException(msg, e);
        }
    }

    @Override
    public Key generateKey() {
        return generateKeyPair().getPublic();
    }

    /**
     * 生成密钥对
     *
     * @return 密钥对
     */
    public KeyPair generateKeyPair() {
        return generateKeyPair(getKeySize());
    }

    /**
     * 生成密钥对
     *
     * @param keySize 密钥大小
     * @return 密钥对
     */
    public KeyPair generateKeyPair(int keySize) {

        KeyPairGenerator keyGenerator;

        try {
            keyGenerator = KeyPairGenerator.getInstance(getAlgorithmName());
        } catch (NoSuchAlgorithmException e) {
            String msg = "非对称加密不支持 " + getAlgorithmName() + " 算法";
            throw new UnknownAlgorithmException(msg, e);
        }

        keyGenerator.initialize(keySize);

        return keyGenerator.generateKeyPair();
    }

    @Override
    protected Key getCipherSecretKey(int mode, byte[] key, String algorithmName) {
        if (mode == Cipher.ENCRYPT_MODE) {
            return getPublicKey(key);
        } else {
            return getPrivateKey(key);
        }

    }

    /**
     * 获取公共密钥
     *
     * @param key 密钥值
     * @return 公共密钥
     */
    protected abstract PublicKey getPublicKey(byte[] key);

    /**
     * 获取私有密钥
     *
     * @param key 密钥值
     * @return 私有密钥
     */
    protected abstract PrivateKey getPrivateKey(byte[] key);

    /**
     * 获取签名算法名称
     *
     * @return 算法名称
     */
    public String getSignatureAlgorithmName() {
        return signatureAlgorithmName;
    }

    /**
     * 设置签名算法名称
     *
     * @param signatureAlgorithmName 算法名称
     */
    public void setSignatureAlgorithmName(String signatureAlgorithmName) {
        this.signatureAlgorithmName = signatureAlgorithmName;
    }

}
