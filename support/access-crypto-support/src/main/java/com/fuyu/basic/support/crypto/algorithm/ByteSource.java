
package com.fuyu.basic.support.crypto.algorithm;

/**
 * 字节原实体类，该类包装一个 byte 数组，提供额外的编码操作
 *
 * @author maurice
 */
public interface ByteSource {

    /**
     * 获取 byte 数组
     *
     * @return byte 数组
     */
    byte[] obtainBytes();

    /**
     * 获取本字节原的 16 进制字符串
     *
     * @return 16 进制字符串
     */
    String getHex();

    /**
     * 获取本字节原的 base64 字符串
     *
     * @return base64 字符串
     */
    String getBase64();

    /**
     * 判断是否为空字节原，判断原则为:
     * <pre>
     * 1.如果字节的长度为0时，表示为空
     * 2.如果为字节为 null 时，表示为空
     * </pre>
     *
     * @return true 表示是，否则 false
     */
    boolean isEmpty();
}
