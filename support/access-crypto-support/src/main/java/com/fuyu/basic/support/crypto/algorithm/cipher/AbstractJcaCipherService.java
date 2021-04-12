
package com.fuyu.basic.support.crypto.algorithm.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.RandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SecureRandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SimpleByteSource;
import com.fuyu.basic.support.crypto.algorithm.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;

/**
 * java 远程加密解密规范的抽象实现
 *
 * @author maurice
 */
public abstract class AbstractJcaCipherService implements CipherService {

    /**
     * 默认密随机数生成器的算法名称
     */
    public static final String RANDOM_NUM_GENERATOR_ALGORITHM_NAME = "SHA1PRNG";

    /**
     * 默认的流缓存区大小
     */
    public static final int DEFAULT_STREAMING_BUFFER_SIZE = 512;

    /**
     * 初始化向量的背数值
     */
    public static final int IV_MULTIPLE_VALUE = 8;

    /**
     * 密钥大小
     */
    private int keySize;

    /**
     * 初始化向量大小
     */
    private int initializationVectorSize = keySize;

    /**
     * 流缓存区大小
     */
    private int streamingBufferSize = DEFAULT_STREAMING_BUFFER_SIZE;

    /**
     * 密随机数生成器
     */
    private RandomNumberGenerator randomNumberGenerator;

    /**
     * 算法名称
     */
    private String algorithmName;

    /**
     * 抽象的密码服务实现
     *
     * @param algorithmName 算法名称
     */
    protected AbstractJcaCipherService(String algorithmName) {
        this.algorithmName = algorithmName;
        SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
        secureRandomNumberGenerator.setSecureRandom(getDefaultSecureRandom());
        randomNumberGenerator = secureRandomNumberGenerator;
    }

    @Override
    public ByteSource decrypt(byte[] cipherText, byte[] key) throws CryptoException {

        byte[] encrypted = cipherText;

        byte[] iv = null;

        if (isGenerateInitializationVectors()) {
            try {

                // 由于密文使用了 iv，所以密文参数数组实际上并不是100％的密文。
                // 它前N个字节是初始化向量的内容，其中 N 等于该值的 initializationVectorSize 属性
                // 方法参数（cipherText.length - N）中的剩余字节才是真实的密文
                // 因此，需要截断 iv 的 N 个长度后才能得到真正的密文

                int ivSize = getInitializationVectorSize();
                int ivByteSize = ivSize / IV_MULTIPLE_VALUE;

                // 获取 iv 长度
                iv = new byte[ivByteSize];
                System.arraycopy(cipherText, 0, iv, 0, ivByteSize);

                // 剩余的数据是实际的加密密文
                int encryptedSize = cipherText.length - ivByteSize;
                encrypted = new byte[encryptedSize];
                System.arraycopy(cipherText, ivByteSize, encrypted, 0, encryptedSize);
            } catch (Exception e) {
                String msg = "无法正确提取初始化向量或密文";
                throw new CryptoException(msg, e);
            }
        }

        return decrypt(encrypted, key, iv);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param key        密钥
     * @param iv         向量值
     * @return 解密后的字节原
     */
    private ByteSource decrypt(byte[] cipherText, byte[] key, byte[] iv) {
        byte[] decrypted = crypt(cipherText, key, iv, Cipher.DECRYPT_MODE);
        return decrypted == null ? null : new SimpleByteSource(decrypted);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out, byte[] key) throws CryptoException {
        byte[] iv = null;

        if (isGenerateInitializationVectors()) {

            // 由于密文使用了 iv，所以密文参数数组实际上并不是100％的密文。
            // 它前N个字节是初始化向量的内容，其中 N 等于该值的 initializationVectorSize 属性
            // 方法参数（cipherText.length - N）中的剩余字节才是真实的密文
            // 因此，需要截断 iv 的 N 个长度后才能得到真正的密文

            int ivSize = getInitializationVectorSize();
            int ivByteSize = ivSize / IV_MULTIPLE_VALUE;
            iv = new byte[ivByteSize];
            int read;

            try {
                read = in.read(iv);
            } catch (IOException e) {
                String msg = "从 input stream 无法正确提取初始化向量";
                throw new CryptoException(msg, e);
            }

            if (read != ivByteSize) {
                throw new CryptoException("在加密钱使用了初始化向量，但从 input stream 无法正确提取初始化向量");
            }
        }

        decrypt(in, out, key, iv);
    }

    /**
     * 加密内容
     *
     * @param in  需要解密的内容输入流
     * @param out 解密后需要返回明文的输出流
     * @param key 密钥
     * @param iv  向量值
     * @throws CryptoException 加密错误时抛出
     */
    private void decrypt(InputStream in, OutputStream out, byte[] key, byte[] iv) throws CryptoException {
        crypt(in, out, key, iv, Cipher.DECRYPT_MODE);
    }

    @Override
    public ByteSource encrypt(byte[] plainText, byte[] key) throws CryptoException {
        byte[] ivBytes = null;

        if (isGenerateInitializationVectors()) {
            ivBytes = generateInitializationVector(false);
            if (ivBytes == null || ivBytes.length == 0) {
                String msg = "生成的向量值为空";
                throw new IllegalStateException(msg);
            }
        }

        return encrypt(plainText, key, ivBytes);
    }

    @Override
    public void encrypt(InputStream in, OutputStream out, byte[] key) throws CryptoException {
        byte[] ivBytes = null;

        if (isGenerateInitializationVectors()) {
            ivBytes = generateInitializationVector(true);
            if (ivBytes == null || ivBytes.length == 0) {
                String msg = "生成的向量值为空";
                throw new IllegalStateException(msg);
            }
        }

        encrypt(in, out, key, ivBytes);
    }

    /**
     * 加密
     *
     * @param in  需要加密的内容输入流
     * @param out 加密后需要返回明文的输出流
     * @param key 密钥
     * @param iv  向量值
     * @throws CryptoException 加密出错时抛出
     */
    private void encrypt(InputStream in, OutputStream out, byte[] key, byte[] iv) throws CryptoException {
        if (iv != null && iv.length > 0) {
            try {
                out.write(iv);
            } catch (IOException e) {
                throw new CryptoException(e);
            }
        }

        crypt(in, out, key, iv, Cipher.ENCRYPT_MODE);
    }

    /**
     * 加密
     *
     * @param plaintext 需要加密的内容
     * @param key       密钥
     * @param iv        向量值
     * @return 加密后的字节原
     */
    protected ByteSource encrypt(byte[] plaintext, byte[] key, byte[] iv) {

        byte[] output;

        if (iv != null && iv.length > 0) {
            // 加密
            byte[] encrypted = crypt(plaintext, key, iv, Cipher.ENCRYPT_MODE);
            output = new byte[iv.length + encrypted.length];

            // 通过 iv + encrypted 复制到 output
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
        } else {
            output = crypt(plaintext, key, iv, Cipher.ENCRYPT_MODE);
        }

        return new SimpleByteSource(output);
    }

    /**
     * 生成向量
     *
     * @param streaming 是否为调用 encrypt(InputStream in, OutputStream out, byte[] encryptionKey) 方法
     * @return 向量字节
     */
    protected byte[] generateInitializationVector(boolean streaming) {
        int size = getInitializationVectorSize();
        if (size <= 0) {
            String msg = "初始化向量值必须大于0";
            throw new IllegalStateException(msg);
        }
        if (size % IV_MULTIPLE_VALUE != 0) {
            String msg = "向量值必须是8个倍数";
            throw new IllegalStateException(msg);
        }
        int sizeInBytes = size / IV_MULTIPLE_VALUE;
        byte[] ivBytes = new byte[sizeInBytes];
        getDefaultSecureRandom().nextBytes(ivBytes);
        return ivBytes;
    }

    /**
     * 加密或解密
     *
     * @param plaintext 文本内容
     * @param key       密钥
     * @param iv        向量值
     * @param mode      加密模型
     * @return 加密或解密的字节数组
     */
    protected byte[] crypt(byte[] plaintext, byte[] key, byte[] iv, int mode) {

        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("key 参数不能为空");
        }

        CipherInfo cipherInfo = initNewCipher(mode, key, iv, false);

        return crypt(cipherInfo, plaintext);
    }

    /**
     * 加密或解密
     *
     * @param in   需要 cipher 的内容输入流
     * @param out  cipher 之后的内容输出流
     * @param key  密钥
     * @param iv   向量值
     * @param mode cipher 模型
     * @throws CryptoException 加密或解密错误时抛出
     */
    private void crypt(InputStream in, OutputStream out, byte[] key, byte[] iv, int mode) throws CryptoException {

        if (in == null) {
            throw new NullPointerException("InputStream 参数不能为 null.");
        }
        if (out == null) {
            throw new NullPointerException("OutputStream 参数不能为 null.");
        }

        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

            byte[] buffer = new byte[getStreamingBufferSize()];

            int n;
            while ((n = in.read(buffer, 0, buffer.length)) > 0) {
                byteArray.write(buffer, 0, n);
            }

            byte[] plaintext = byteArray.toByteArray();
            byte[] result = crypt(plaintext, key, iv, mode);

            out.write(result, 0, result.length);
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 加密或解密
     *
     * @param cipherInfo 暗号信息类
     * @param text       文本内容
     * @return 加密或解密后的字节
     */
    private byte[] crypt(CipherInfo cipherInfo, byte[] text) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 文本长度
        int textLength = text.length;

        byte[] cache;

        try {

            if (AbstractAsymmetricCipherService.class.isAssignableFrom(getClass())) {
                RSAKey rsaPublicKey = (RSAKey) cipherInfo.getKey();
                // 如果非对称加密，需要分段加解密，所以先求出分段开大小在做加解密
                int blockSize = rsaPublicKey.getModulus().bitLength() / AbstractAsymmetricCipherService.DEFAULT_BLOCK_SIZE_MULTIPLE;

                // 如果是加密，keySize <= 1024 时，需要使用128 - 11做块单位
                if (Cipher.ENCRYPT_MODE == cipherInfo.getMode()) {
                    // 由于加密时，要求位数必须比明文长度少11位，这里直接减
                    blockSize = blockSize - AbstractAsymmetricCipherService.DEFAULT_ENCRYPT_ROUNDING_DIGIT;
                }

                // 循环做块的加解密，首次设置的offSet(偏移量)为0. 当文本内容小于 blockSize 时，
                // 直接 doFinal 完成加解密即可，否则得出每次循环偏移量后，在通偏移量定位每次取值的范围。
                for (int i = 0, offSet = 0; textLength - offSet > 0; i++, offSet = i * blockSize) {

                    if (textLength - offSet > blockSize) {
                        cache = cipherInfo.getCipher().doFinal(text, offSet, blockSize);
                    } else {
                        cache = cipherInfo.getCipher().doFinal(text, offSet, textLength - offSet);
                    }
                    out.write(cache, 0, cache.length);
                }

                out.flush();
                out.close();

                return out.toByteArray();
            } else {
                return cipherInfo.getCipher().doFinal(text);
            }
        } catch (Exception e) {
            String msg = "无法执行 " + Cipher.class.getName() + ".doFinal 方法";
            throw new CryptoException(msg, e);
        }
    }

    /**
     * 初始化一个新的 Cipher 实例
     *
     * @param mode      创建模型
     * @param key       密钥
     * @param iv        向量值
     * @param streaming 是否为调用 encrypt(InputStream in, OutputStream out, byte[] encryptionKey) 方法
     * @return 一个新的 Cipher 实例
     */
    private CipherInfo initNewCipher(int mode, byte[] key, byte[] iv, boolean streaming) {
        String transformationString = getCipherTransformation(streaming);

        Key jdkKey = getCipherSecretKey(mode, key, getAlgorithmName());
        SecureRandom secureRandom = getRandomNumberGenerator().getRandom();

        if (iv != null && iv.length > 0) {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            return new CipherInfo(mode, transformationString, jdkKey, ivSpec, secureRandom);
        }

        //initCipher(cipher, mode, jdkKey, ivSpec, getRandomNumberGenerator());

        return new CipherInfo(mode, transformationString, jdkKey, secureRandom);
    }

    /**
     * 获取一个密钥
     *
     * @param mode          Cipher 的 MODE 值
     * @param key           密钥字节数组
     * @param algorithmName 算法名称
     * @return 密钥接口
     */
    protected abstract Key getCipherSecretKey(int mode, byte[] key, String algorithmName);

    /**
     * 是否生成初始化向量
     *
     * @return true 标识是，否则 false
     */
    protected boolean isGenerateInitializationVectors() {
        return getInitializationVectorSize() > 0;
    }

    /**
     * 获取 jca 的 Cipher 转型名称
     *
     * @param streaming 是否为调用 encrypt(InputStream in, OutputStream out, byte[] encryptionKey) 方法
     * @return 转型名称
     */
    protected String getCipherTransformation(boolean streaming) {
        return getAlgorithmName();
    }


    /**
     * 获取流的缓存区大小
     *
     * @return 缓冲区大小
     */
    private int getStreamingBufferSize() {
        return streamingBufferSize;
    }

    /**
     * 设置流的缓存区大小
     *
     * @param streamingBufferSize 缓冲区大小
     */
    public void setStreamingBufferSize(int streamingBufferSize) {
        this.streamingBufferSize = streamingBufferSize;
    }

    /**
     * 获取密钥大小
     *
     * @return 密钥大小
     */
    public int getKeySize() {
        return keySize;
    }

    /**
     * 设置密钥大小
     *
     * @param keySize 密钥大小
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    /**
     * 获取初始化的向量值大小
     *
     * @return 向量值大小
     */
    public int getInitializationVectorSize() {
        return initializationVectorSize;
    }

    /**
     * 设置初始化的向量值大小
     *
     * @param initializationVectorSize 向量值大小
     */
    public void setInitializationVectorSize(int initializationVectorSize) {
        this.initializationVectorSize = initializationVectorSize;
    }

    /**
     * 获取默认的随机生成器
     *
     * @return 随机生成器
     */
    protected static SecureRandom getDefaultSecureRandom() {
        try {
            return SecureRandom.getInstance(RANDOM_NUM_GENERATOR_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }

    /**
     * 获取随机生成器
     *
     * @return 随机生成器
     */
    public RandomNumberGenerator getRandomNumberGenerator() {
        return randomNumberGenerator;
    }

    /**
     * 设置随机生成器
     *
     * @param randomNumberGenerator 随机生成器
     */
    public void setRandomNumberGenerator(RandomNumberGenerator randomNumberGenerator) {
        this.randomNumberGenerator = randomNumberGenerator;
    }

    /**
     * 获取算法名称
     *
     * @return 算法名称
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * 设置算法名称
     *
     * @param algorithmName 算法名称
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }
}
