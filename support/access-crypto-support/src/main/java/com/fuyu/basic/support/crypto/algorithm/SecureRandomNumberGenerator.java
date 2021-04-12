
package com.fuyu.basic.support.crypto.algorithm;

import java.security.SecureRandom;

/**
 * {@link SecureRandom} 的随机数值生成期实现
 *
 * @author maurice
 */
public class SecureRandomNumberGenerator implements RandomNumberGenerator {

    /**
     * 默认下个随机值的字节大小
     */
    protected static final int DEFAULT_NEXT_BYTES_SIZE = 16;

    /**
     * 下个随机值的字节大小
     */
    private int nextBytesSize = DEFAULT_NEXT_BYTES_SIZE;
    /**
     * jdk SecureRandom
     */
    private SecureRandom secureRandom = new SecureRandom();

    /**
     * {@link SecureRandom} 的随机数值生成期实现
     */
    public SecureRandomNumberGenerator() {
    }

    /**
     * 设置随机种子值
     *
     * @param bytes 种子值
     */
    public void setSeed(byte[] bytes) {
        this.secureRandom.setSeed(bytes);
    }

    /**
     * 设置 jdk SecureRandom
     *
     * @param random {@link SecureRandom}
     * @throws IllegalArgumentException random 参数为 null 时抛出
     */
    public void setSecureRandom(SecureRandom random) throws IllegalArgumentException {
        if (random == null) {
            throw new IllegalArgumentException("SecureRandom 参数不能为 null");
        }
        this.secureRandom = random;
    }

    /**
     * 获取下个随机值的字节大小
     *
     * @return 下个随机值的字节大小
     */
    public int getNextBytesSize() {
        return nextBytesSize;
    }

    /**
     * 是个直下个随机值的字节大小
     *
     * @param nextBytesSize 下个随机值的字节大小
     * @throws IllegalArgumentException 下个随机值的字节大小小于等于 0 时抛出
     */
    public void setNextBytesSize(int nextBytesSize) throws IllegalArgumentException {
        if (nextBytesSize <= 0) {
            throw new IllegalArgumentException("nextBytesSize 参数必须大于 0");
        }
        this.nextBytesSize = nextBytesSize;
    }

    @Override
    public ByteSource nextBytes() {
        return nextBytes(getNextBytesSize());
    }

    @Override
    public ByteSource nextBytes(int numBytes) {
        if (numBytes <= 0) {
            throw new IllegalArgumentException("numBytes argument 参数必须大于 0");
        }
        byte[] bytes = new byte[numBytes];
        this.secureRandom.nextBytes(bytes);
        return new SimpleByteSource(bytes);
    }

    @Override
    public SecureRandom getRandom() {
        return this.secureRandom;
    }
}
