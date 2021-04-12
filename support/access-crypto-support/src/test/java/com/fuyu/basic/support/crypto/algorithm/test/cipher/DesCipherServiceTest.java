

package com.fuyu.basic.support.crypto.algorithm.test.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.CodecUtils;
import com.fuyu.basic.support.crypto.algorithm.cipher.DesCipherService;
import com.fuyu.basic.support.crypto.algorithm.test.TestData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author maurice
 */
public class DesCipherServiceTest {

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    public void test() throws NoSuchAlgorithmException, IOException {

        DesCipherService cipherService = new DesCipherService();

        byte[] key = cipherService.generateKey().getEncoded();

        ByteSource source = cipherService.encrypt(TestData.TEXT.getBytes(), key);
        ByteSource target = cipherService.decrypt(source.obtainBytes(), key);

        Assert.assertTrue(Arrays.equals(target.obtainBytes(), TestData.TEXT.getBytes()));

        ByteArrayOutputStream encryptOut = new ByteArrayOutputStream();
        InputStream encryptIn = resourceLoader.getResource("classpath:/data.test").getInputStream();

        cipherService.encrypt(encryptIn, encryptOut, key);

        InputStream decryptIn = new ByteArrayInputStream(encryptOut.toByteArray());
        ByteArrayOutputStream decryptOut = new ByteArrayOutputStream();
        cipherService.decrypt(decryptIn, decryptOut, key);

        byte[] text = CodecUtils.toBytes(resourceLoader.getResource("classpath:/data.test").getInputStream());
        Assert.assertTrue(Arrays.equals(decryptOut.toByteArray(), text));
    }
}
