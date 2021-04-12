
package com.fuyu.basic.support.crypto.algorithm.test.hash;

import com.fuyu.basic.support.crypto.algorithm.SimpleByteSource;
import com.fuyu.basic.support.crypto.algorithm.hash.Hash;
import com.fuyu.basic.support.crypto.algorithm.hash.HashAlgorithmMode;
import com.fuyu.basic.support.crypto.algorithm.hash.HashRequest;
import com.fuyu.basic.support.crypto.algorithm.hash.HashService;
import com.fuyu.basic.support.crypto.algorithm.test.TestData;
import org.junit.Test;

/**
 * hash 服务单元测试
 *
 * @author maurice
 */
public class HashServiceTest {

    private HashService hashService = new HashService();

    @Test
    public void testComputeHash() {
        Hash sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT)));
        Hash targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT)));

        TestData.assertHash(sourceHash, targetHash, 32);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA1.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA1.getName()));

        TestData.assertHash(sourceHash, targetHash, 40);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA256.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA256.getName()));

        TestData.assertHash(sourceHash, targetHash, 64);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA384.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA384.getName()));

        TestData.assertHash(sourceHash, targetHash, 96);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA512.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), HashAlgorithmMode.SHA512.getName()));

        TestData.assertHash(sourceHash, targetHash, 128);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT)));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT)));

        TestData.assertHash(sourceHash, targetHash, 32);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA1.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA1.getName()));

        TestData.assertHash(sourceHash, targetHash, 40);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA256.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA256.getName()));

        TestData.assertHash(sourceHash, targetHash, 64);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA384.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA384.getName()));

        TestData.assertHash(sourceHash, targetHash, 96);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA512.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), HashAlgorithmMode.SHA512.getName()));

        TestData.assertHash(sourceHash, targetHash, 128);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3));

        TestData.assertHash(sourceHash, targetHash, 32);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA1.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA1.getName()));

        TestData.assertHash(sourceHash, targetHash, 40);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA256.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA256.getName()));

        TestData.assertHash(sourceHash, targetHash, 64);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA384.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA384.getName()));

        TestData.assertHash(sourceHash, targetHash, 96);

        sourceHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA512.getName()));
        targetHash = hashService.computeHash(new HashRequest(new SimpleByteSource(TestData.TEXT), new SimpleByteSource(TestData.SALT), 3, HashAlgorithmMode.SHA512.getName()));

        TestData.assertHash(sourceHash, targetHash, 128);
    }

}

