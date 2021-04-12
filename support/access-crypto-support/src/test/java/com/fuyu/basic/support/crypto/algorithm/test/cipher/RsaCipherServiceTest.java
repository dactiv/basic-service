

package com.fuyu.basic.support.crypto.algorithm.test.cipher;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.cipher.RsaCipherService;
import com.fuyu.basic.support.crypto.algorithm.test.TestData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 非对称加密单元测试
 *
 * @author maurice
 */
public class RsaCipherServiceTest {

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    public void test() throws NoSuchAlgorithmException, IOException {
        RsaCipherService cipherService = new RsaCipherService();
        cipherService.setKeySize(2048);

        KeyPair keyPair = cipherService.generateKeyPair();

        ByteSource source = cipherService.encrypt(TestData.TEXT.getBytes(), keyPair.getPublic().getEncoded());

        ByteSource sign = cipherService.sign(source.obtainBytes(), keyPair.getPrivate().getEncoded());
        Assert.assertTrue(cipherService.verify(source.obtainBytes(), keyPair.getPublic().getEncoded(), sign.obtainBytes()));

        ByteSource target = cipherService.decrypt(source.obtainBytes(), keyPair.getPrivate().getEncoded());

        Assert.assertTrue(Arrays.equals(target.obtainBytes(), TestData.TEXT.getBytes()));

        RsaCipherService miniCipherService = new RsaCipherService();
        miniCipherService.setKeySize(1024);

        ByteArrayOutputStream encryptOut = new ByteArrayOutputStream();
        InputStream encryptIn = resourceLoader.getResource("classpath:/data.test").getInputStream();

        miniCipherService.encrypt(encryptIn, encryptOut, keyPair.getPublic().getEncoded());

        sign = miniCipherService.sign(encryptOut.toByteArray(), keyPair.getPrivate().getEncoded());
        Assert.assertTrue(miniCipherService.verify(encryptOut.toByteArray(), keyPair.getPublic().getEncoded(), sign.obtainBytes()));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptOut.toByteArray());
        ByteArrayOutputStream decryptOut = new ByteArrayOutputStream();

        miniCipherService.decrypt(byteArrayInputStream, decryptOut, keyPair.getPrivate().getEncoded());
        encryptIn = resourceLoader.getResource("classpath:/data.test").getInputStream();
        String text = read(encryptIn);
        Assert.assertTrue(Arrays.equals(decryptOut.toByteArray(), text.getBytes()));
    }

    private String read(InputStream is) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();

    }
}
