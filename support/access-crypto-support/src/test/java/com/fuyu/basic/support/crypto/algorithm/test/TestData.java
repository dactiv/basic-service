
package com.fuyu.basic.support.crypto.algorithm.test;

import com.fuyu.basic.support.crypto.algorithm.Base64;
import com.fuyu.basic.support.crypto.algorithm.hash.Hash;
import org.junit.Assert;

/**
 * @author maurice
 */
public class TestData {

    public static final String TEXT = "1234567890abcdefghijklnmopqrsduvwxyz一串中文后面跟着标点符号~!@#$%^&*()_+-=[]{}\';,./?><:";

    public static final String SALT = "maurice";

    public static void assertHash(Hash source, Hash target, int actualLength) {
        Assert.assertEquals(source.getHex().length(), actualLength);
        Assert.assertTrue(Base64.isBase64(source.getBase64().getBytes()));
        Assert.assertTrue(Base64.isBase64(source.toString().getBytes()));

        Assert.assertEquals(target.getHex().length(), actualLength);
        Assert.assertTrue(Base64.isBase64(target.getBase64().getBytes()));
        Assert.assertTrue(Base64.isBase64(target.toString().getBytes()));

        Assert.assertEquals(source, target);
    }
}
