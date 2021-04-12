
package com.fuyu.basic.support.crypto.algorithm;

import com.fuyu.basic.support.crypto.algorithm.exception.CodecException;

import java.io.*;

/**
 * 编码器工具类
 *
 * @author maurice
 */
public class CodecUtils {

    /**
     * 默认的字节编码
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 默认的缓冲区大小
     */
    private static final int DEFAULT_BUFFER_SIZE = 512;

    /**
     * 将 char 数组转型成字节数组
     *
     * @param chars char 数组
     * @return 字节数组
     */
    public static byte[] toBytes(char[] chars) {
        return toBytes(new String(chars), DEFAULT_ENCODING);
    }

    /**
     * 将 char 数组类型转型成字节数组
     *
     * @param chars    char 数组
     * @param encoding 内容编码
     * @return 字节数组
     * @throws CodecException 编码出错时抛出
     */
    public static byte[] toBytes(char[] chars, String encoding) throws CodecException {
        return toBytes(new String(chars), encoding);
    }

    /**
     * 将 string 类型转型成字节数组
     *
     * @param source 字符串
     * @return 字节数组
     */
    public static byte[] toBytes(String source) {
        return toBytes(source, DEFAULT_ENCODING);
    }

    /**
     * 将 string 类型转型成字节数组
     *
     * @param source   字符串
     * @param encoding 内容编码
     * @return 字节数组
     * @throws CodecException 编码出错时抛出
     */
    public static byte[] toBytes(String source, String encoding) throws CodecException {
        try {
            return source.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            String msg = "使用 " + encoding + " 无法转换[" + source + "] 为 byte数组 ";
            throw new CodecException(msg, e);
        }
    }

    /**
     * 将字节数组转型成 String 类型
     *
     * @param bytes 字节数组
     * @return string 类型内容
     */
    public static String toString(byte[] bytes) {
        return toString(bytes, DEFAULT_ENCODING);
    }

    /**
     * 将字节数组转型成 String 类型
     *
     * @param bytes    字节数组
     * @param encoding 内容编码
     * @return string 类型内容
     * @throws CodecException 编码出错时抛出
     */
    public static String toString(byte[] bytes, String encoding) throws CodecException {
        try {
            return new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
            String msg = "用" + encoding + "无法将 byte 数组转换成 String 类型";
            throw new CodecException(msg, e);
        }
    }

    /**
     * 将字节数组转成成 char 数组
     *
     * @param bytes 字节数组
     * @return char 数组
     */
    public static char[] toChars(byte[] bytes) {
        return toChars(bytes, DEFAULT_ENCODING);
    }

    /**
     * 将字节数组转成成 char 数组
     *
     * @param bytes    字节数组
     * @param encoding 内容编码
     * @return char 数组
     * @throws CodecException 编码出错时抛出
     */
    public static char[] toChars(byte[] bytes, String encoding) throws CodecException {
        return toString(bytes, encoding).toCharArray();
    }

    /**
     * 将 file 类型转型成字节数组
     *
     * @param file file 类型
     * @return 字节数组
     */
    public static byte[] toBytes(File file) {
        return toBytes(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将 file 类型转型成字节数组
     *
     * @param file       file 类型
     * @param bufferSize 初始化流大小
     * @return 字节数组
     */
    public static byte[] toBytes(File file, int bufferSize) {
        if (file == null) {
            throw new IllegalArgumentException("File 参数不能为 null");
        }
        try {
            return toBytes(new FileInputStream(file), bufferSize);
        } catch (FileNotFoundException e) {
            String msg = "无法获取 [" + file + "]";
            throw new CodecException(msg, e);
        }
    }

    /**
     * 将 input 流转型成字节数组
     *
     * @param in input 流
     * @return 字节数组
     */
    public static byte[] toBytes(InputStream in) {
        return toBytes(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将 input 流转型成字节数组
     *
     * @param in         input 流
     * @param bufferSize 初始化流大小
     * @return 字节数组
     */
    public static byte[] toBytes(InputStream in, int bufferSize) {

        if (in == null) {
            throw new IllegalArgumentException("InputStream 参数不能为 null");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        try {
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } catch (IOException ioe) {
            throw new CodecException(ioe);
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {

        }
    }

    /**
     * 将 ByteSource、char[]、String、File、InputStream 转型成字节数组
     *
     * @param o ByteSource、char[]、String、File、InputStream 对象
     * @return 字节数组
     */
    public static byte[] toBytes(Object o) {
        if (o == null) {
            String msg = "参数不能为 null";
            throw new IllegalArgumentException(msg);
        }

        if (o instanceof byte[]) {
            return (byte[]) o;
        } else if (o instanceof ByteSource) {
            return ((ByteSource) o).obtainBytes();
        } else if (o instanceof char[]) {
            return toBytes((char[]) o);
        } else if (o instanceof String) {
            return toBytes((String) o);
        } else if (o instanceof File) {
            return toBytes((File) o);
        } else if (o instanceof InputStream) {
            return toBytes((InputStream) o);
        } else {
            String msg = "本类支持转换 byte 数组类型的对象，对象类型支持 byte[]、" + ByteSource.class.getName() +
                    "、char[]、String、File、InputStream，该参数类型为:" + o.getClass().getName();
            throw new CodecException(msg);
        }
    }
}
