
package com.fuyu.basic.support.crypto.algorithm.hash;

import com.fuyu.basic.support.crypto.algorithm.ByteSource;
import com.fuyu.basic.support.crypto.algorithm.RandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SecureRandomNumberGenerator;
import com.fuyu.basic.support.crypto.algorithm.SimpleByteSource;

/**
 * hash 服务
 *
 * @author maurice
 */
public class HashService {

    /**
     * 随机数值生成器
     */
    private RandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();

    /**
     * hash 算法模型
     */
    private HashAlgorithmMode algorithmMode = HashAlgorithmMode.MD5;

    /**
     * 私有盐
     */
    private ByteSource privateSalt;

    /**
     * hash 迭代值
     */
    private int iterations;

    /**
     * 是否生成公有盐
     */
    private boolean generatePublicSalt;

    /**
     * hash 服务
     */
    public HashService() {
        this.iterations = 1;
        this.generatePublicSalt = false;
        this.randomNumberGenerator = new SecureRandomNumberGenerator();
    }

    /**
     * 计算 hash
     *
     * @param request hash 请求对象
     * @return hash 对象
     */
    public Hash computeHash(HashRequest request) {

        if (request == null || request.getSource() == null || request.getSource().isEmpty()) {
            return null;
        }

        String algorithmName = getAlgorithmName(request);
        ByteSource source = request.getSource();
        int iterations = getIterations(request);

        ByteSource publicSalt = getPublicSalt(request);
        ByteSource privateSalt = getPrivateSalt();
        ByteSource salt = getCombineSalt(privateSalt, publicSalt);

        Hash computed = new Hash(algorithmName, source, salt, iterations);

        Hash result = new Hash(algorithmName, computed.obtainBytes());
        result.setIterations(iterations);
        // 只暴露公开盐，不暴露组合盐
        result.setSalt(publicSalt);

        return result;
    }

    /**
     * 获取公共盐
     *
     * @param request hash 请求
     * @return 公共盐
     */
    protected ByteSource getPublicSalt(HashRequest request) {

        ByteSource publicSalt = request.getSalt();

        if (publicSalt != null && !publicSalt.isEmpty()) {
            // 如果请求对象有公共盐，使用公共盐
            return publicSalt;
        }

        publicSalt = null;

        // 检查是否要生成一个私有盐
        ByteSource privateSalt = getPrivateSalt();
        boolean privateSaltExists = privateSalt != null && !privateSalt.isEmpty();

        // 如果存在私有盐，必须产生公共盐，以保护私有盐的完整性
        if (privateSaltExists || isGeneratePublicSalt()) {
            publicSalt = getRandomNumberGenerator().nextBytes();
        }

        return publicSalt;
    }

    /**
     * 获取组合盐
     *
     * @param privateSalt 私有盐
     * @param publicSalt  共有盐
     * @return 组合盐字节原
     */
    protected ByteSource getCombineSalt(ByteSource privateSalt, ByteSource publicSalt) {

        byte[] privateSaltBytes = privateSalt != null ? privateSalt.obtainBytes() : null;
        int privateSaltLength = privateSaltBytes != null ? privateSaltBytes.length : 0;

        byte[] publicSaltBytes = publicSalt != null ? publicSalt.obtainBytes() : null;
        int extraBytesLength = publicSaltBytes != null ? publicSaltBytes.length : 0;

        int length = privateSaltLength + extraBytesLength;

        if (length <= 0) {
            return null;
        }

        byte[] combined = new byte[length];

        int i = 0;
        for (int j = 0; j < privateSaltLength; j++) {
            combined[i++] = privateSaltBytes[j];
        }
        for (int j = 0; j < extraBytesLength; j++) {
            combined[i++] = publicSaltBytes[j];
        }

        return new SimpleByteSource(combined);
    }

    /**
     * 获取算法名称
     *
     * @param request hash 请求
     * @return 算法名称
     */
    protected String getAlgorithmName(HashRequest request) {
        String name = request.getAlgorithmName();
        if (name == null) {
            name = getAlgorithmMode().getName();
        }
        return name;
    }

    /**
     * 获取 hash 迭代次数
     *
     * @param request hash 请求
     * @return 迭代次数
     */
    protected int getIterations(HashRequest request) {
        int iterations = Math.max(0, request.getIterations());
        if (iterations < 1) {
            iterations = Math.max(1, getIterations());
        }
        return iterations;
    }

    /**
     * 获取随机数值生成器
     *
     * @return 随机数值生成器
     */
    public RandomNumberGenerator getRandomNumberGenerator() {
        return randomNumberGenerator;
    }

    /**
     * 设置随机数值生成器
     *
     * @param randomNumberGenerator 随机数值生成器
     */
    public void setRandomNumberGenerator(RandomNumberGenerator randomNumberGenerator) {
        this.randomNumberGenerator = randomNumberGenerator;
    }

    /**
     * 获取 hash 算法模式
     *
     * @return hash 算法模式
     */
    public HashAlgorithmMode getAlgorithmMode() {
        return algorithmMode;
    }

    /**
     * 设置 hash 算法模式
     *
     * @param algorithmMode hash 算法模式
     */
    public void setAlgorithmMode(HashAlgorithmMode algorithmMode) {
        this.algorithmMode = algorithmMode;
    }

    /**
     * 获取私有盐
     *
     * @return 私有盐
     */
    public ByteSource getPrivateSalt() {
        return privateSalt;
    }

    /**
     * 设置私有盐
     *
     * @param privateSalt 私有盐
     */
    public void setPrivateSalt(ByteSource privateSalt) {
        this.privateSalt = privateSalt;
    }

    /**
     * 获取 hash 迭代次数
     *
     * @return 迭代次数
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * 设置 hash 迭代次数
     *
     * @param iterations 迭代次数
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * 判断每次 hash 是否都生成共有盐
     *
     * @return true 表示是，否则 false
     */
    public boolean isGeneratePublicSalt() {
        return generatePublicSalt;
    }

    /**
     * 设置每次 hash 是否都生成共有盐
     *
     * @param generatePublicSalt true 表示是，否则 false
     */
    public void setGeneratePublicSalt(boolean generatePublicSalt) {
        this.generatePublicSalt = generatePublicSalt;
    }
}
