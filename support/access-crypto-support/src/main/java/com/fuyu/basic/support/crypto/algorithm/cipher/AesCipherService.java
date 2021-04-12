

package com.fuyu.basic.support.crypto.algorithm.cipher;

/**
 * AES 对称加密实现，特点：密钥建立时间短、灵敏性好、内存需求低、安全性高
 *
 * @author maurice
 */
public class AesCipherService extends SymmetricCipherService {

    private static final int DEFAULT_KEY_SIZE = 128;

    /**
     * AES 对称加密实现
     */
    public AesCipherService() {
        super("AES");

        setKeySize(DEFAULT_KEY_SIZE);
        setInitializationVectorSize(DEFAULT_KEY_SIZE);

        setMode(OperationMode.CBC);
        setPaddingScheme(PaddingScheme.PKCS5);

        setStreamingMode(OperationMode.CBC);
        setStreamingPaddingScheme(PaddingScheme.PKCS5);
    }
}
