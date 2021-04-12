

package com.fuyu.basic.support.crypto.algorithm.cipher;

/**
 * DES 对称加密实现，特点：密钥偏短（56位）、生命周期短（避免被破解）
 *
 * @author maurice
 */
public class DesCipherService extends SymmetricCipherService {

    /**
     * 默认密钥大小
     */
    private static final int DEFAULT_KEY_SIZE = 56;

    /**
     * 默认初始化向量大小
     */
    private static final int DEFAULT_INITIALIZATION_VECTOR_SIZE = 64;

    /**
     * DES 对称加密实现
     */
    public DesCipherService() {
        super("DES");
        setKeySize(DEFAULT_KEY_SIZE);
        setInitializationVectorSize(DEFAULT_INITIALIZATION_VECTOR_SIZE);

        setMode(OperationMode.CBC);
        setPaddingScheme(PaddingScheme.PKCS5);

        setStreamingMode(OperationMode.CBC);
        setStreamingPaddingScheme(PaddingScheme.PKCS5);
    }
}
