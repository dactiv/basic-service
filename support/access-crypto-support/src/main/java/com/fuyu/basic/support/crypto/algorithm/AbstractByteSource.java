
package com.fuyu.basic.support.crypto.algorithm;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 抽象的字节原实现，该抽象对获取本字节原的 16 进制字符串和 base64 字符串做了一次缓存，在当前存在
 * 字节原的 16 进制字符串和 base64 字符串时，不会在获取一次字节数组在做一次转换
 * <p>
 * 主要实现了以下方法:
 * <pre>
 * {@link #getHex()}
 * {@link #getBase64()}
 * {@link #isEmpty()}
 * </pre>
 * <p>
 * 重写了以下方法:
 * <pre>
 * {@link #toString()},使用 {@link #getBase64()} 做返回值
 * {@link #hashCode()},使用 {@link Arrays#hashCode(byte[])} 做返回值
 * {@link #equals(Object)} 通过 {@link ByteSource#obtainBytes()} 去做对比，对比使用 {@link Arrays#equals(byte[], byte[])}
 * </pre>
 *
 * @author maurice
 */
public abstract class AbstractByteSource implements ByteSource, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 缓存 16 进制值的变量
     */
    private String cachedHex;
    /**
     * 缓存 base64 值的变量
     */
    private String cachedBase64;

    @Override
    public String getHex() {

        if (cachedHex == null) {
            cachedHex = Hex.encodeToString(obtainBytes());
        }

        return cachedHex;
    }

    @Override
    public String getBase64() {
        if (cachedBase64 == null) {
            cachedBase64 = Base64.encodeToString(obtainBytes());
        }

        return cachedBase64;
    }

    @Override
    public boolean isEmpty() {
        return this.obtainBytes() == null || this.obtainBytes().length == 0;
    }

    @Override
    public String toString() {
        return getBase64();
    }

    @Override
    public int hashCode() {
        if (isEmpty()) {
            return 0;
        }
        return Arrays.hashCode(this.obtainBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ByteSource) {
            ByteSource bs = (ByteSource) o;
            return Arrays.equals(obtainBytes(), bs.obtainBytes());
        }
        return false;
    }
}
