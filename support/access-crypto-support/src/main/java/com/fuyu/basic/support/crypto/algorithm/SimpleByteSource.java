
package com.fuyu.basic.support.crypto.algorithm;

import java.io.File;
import java.io.InputStream;

/**
 * 简单的字节原实现
 *
 * @author maurice
 */
public class SimpleByteSource extends AbstractByteSource {

    /**
     * 字节数组
     */
    private byte[] bytes;

    /**
     * 简单的字节原实现
     *
     * @param o ByteSource、char[]、String、File、InputStream 对象
     */
    public SimpleByteSource(Object o) {
        this(CodecUtils.toBytes(o));
    }

    /**
     * 简单的字节原实现
     *
     * @param bytes 字节数组
     */
    public SimpleByteSource(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * 简单的字节原实现
     *
     * @param chars 字符数组
     */
    public SimpleByteSource(char[] chars) {
        this(CodecUtils.toBytes(chars));
    }

    /**
     * 简单的字节原实现
     *
     * @param string 字符串
     */
    public SimpleByteSource(String string) {
        this(CodecUtils.toBytes(string));
    }

    /**
     * 简单的字节原实现
     *
     * @param source 字节原实体类
     */
    public SimpleByteSource(ByteSource source) {
        this(source.obtainBytes());
    }

    /**
     * 简单的字节原实现
     *
     * @param source 字节原实体类
     */
    public SimpleByteSource(File source) {
        this(CodecUtils.toBytes(source));
    }

    /**
     * 简单的字节原实现
     *
     * @param source 字节原实体类
     */
    public SimpleByteSource(InputStream source) {
        this(CodecUtils.toBytes(source));
    }

    /**
     * 简单的字节原实现
     *
     * @param source     字节原实体类
     * @param bufferSize 初始化流大小
     */
    public SimpleByteSource(File source, int bufferSize) {
        this(CodecUtils.toBytes(source, bufferSize));
    }

    /**
     * 简单的字节原实现
     *
     * @param source     字节原实体类
     * @param bufferSize 初始化流大小
     */
    public SimpleByteSource(InputStream source, int bufferSize) {
        this(CodecUtils.toBytes(source, bufferSize));
    }

    @Override
    public byte[] obtainBytes() {
        return bytes;
    }
}
