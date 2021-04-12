

package com.fuyu.basic.support.crypto.algorithm.cipher;

/**
 * 块密码模型的抽象实现
 *
 * @author maurice
 */
public abstract class AbstractBlockCipherService extends AbstractJcaCipherService implements CipherKeyGenerator {

    /**
     * 默认的块大小
     */
    public static final int DEFAULT_BLOCK_SIZE = 0;

    /**
     * 转型字符串分隔符
     */
    private static final String TRANSFORMATION_STRING_DELIMITER = "/";

    /**
     * 默认以流形式传入处理的块大小
     */
    public static final int DEFAULT_STREAMING_BLOCK_SIZE = 8;

    /**
     * 密码分组模式
     */
    private OperationMode mode;

    /**
     * 块大小
     */
    private int blockSize = DEFAULT_BLOCK_SIZE;

    /**
     * 填充方案
     */
    private PaddingScheme paddingScheme;

    /**
     * 以流加密解密的分组模型
     */
    private OperationMode streamingMode;
    /**
     * 以流加密解密的块大小
     */
    private int streamingBlockSize = DEFAULT_STREAMING_BLOCK_SIZE;
    /**
     * 以流加密解密的填充方案名称
     */
    private PaddingScheme streamingPaddingScheme;

    /**
     * 缓存转型字符串名称，如果发生变化是在重新缓存
     */
    private String transformationString;
    /**
     * 缓存以流加密解密转型字符串名称，如果发生变化是在重新缓存
     */
    private String streamingTransformationString;

    /**
     * 抽象的密码服务实现
     *
     * @param algorithmName 算法名称
     */
    AbstractBlockCipherService(String algorithmName) {
        super(algorithmName);
    }

    /**
     * 绑定转换字符串
     *
     * @return 字符串
     */
    private String buildTransformationString() {
        return buildTransformationString(getMode(), getPaddingScheme(), getBlockSize());
    }

    /**
     * 绑定以流形式加密解密的转换字符串
     *
     * @return 字符串
     */
    private String buildStreamingTransformationString() {
        return buildTransformationString(getMode(), getPaddingScheme(), 0);
    }

    /**
     * 绑定转换字符串
     *
     * @param mode      密码分组模式
     * @param scheme    填充方案
     * @param blockSize 块大小
     * @return 字符串
     */
    private String buildTransformationString(OperationMode mode, PaddingScheme scheme, int blockSize) {
        StringBuilder sb = new StringBuilder(getAlgorithmName());
        if (mode != null) {
            sb.append(TRANSFORMATION_STRING_DELIMITER).append(mode.name());
        }
        if (blockSize > 0) {
            sb.append(blockSize);
        }
        if (scheme != null) {
            sb.append(TRANSFORMATION_STRING_DELIMITER).append(scheme.getTransformationName());
        }
        return sb.toString();
    }

    /**
     * 判断密码分组模式是否支持初始化向量
     *
     * @param mode 密码分组模式
     * @return true 为是，否则 false
     */
    private boolean isModeInitializationVectorSupport(OperationMode mode) {
        return mode != null && !mode.equals(OperationMode.ECB) && !mode.equals(OperationMode.NONE);
    }

    /**
     * 重写父类方法，在是否以流加密解密中指示本次操作以什么来做 cipher 的初始化转型字符串
     */
    @Override
    protected String getCipherTransformation(boolean streaming) {
        if (streaming) {
            if (this.streamingTransformationString == null) {
                this.streamingTransformationString = buildStreamingTransformationString();
            }
            return this.streamingTransformationString;
        } else {
            if (this.transformationString == null) {
                this.transformationString = buildTransformationString();
            }
            return this.transformationString;
        }
    }

    /**
     * 重写父类方法，在生成向量前，判断本次的密码分组模式是否支持生成初始化向量
     */
    @Override
    protected byte[] generateInitializationVector(boolean streaming) {
        if (streaming) {
            OperationMode streamingMode = getStreamingMode();
            if (!isModeInitializationVectorSupport(streamingMode)) {
                String msg = "[流]-[" + streamingMode.name() + "]密码分组模式不支持初始化向量";
                throw new IllegalStateException(msg);
            }
        } else {
            OperationMode mode = getMode();
            if (!isModeInitializationVectorSupport(mode)) {
                String msg = "[" + streamingMode.name() + "]密码分组模式不支持初始化向量";
                throw new IllegalStateException(msg);
            }
        }
        return super.generateInitializationVector(streaming);
    }

    /**
     * 获取密码分组模型
     *
     * @return 模型
     */
    public OperationMode getMode() {
        return mode;
    }

    /**
     * 设置密码分组模型
     *
     * @param mode 模型
     */
    public void setMode(OperationMode mode) {
        this.mode = mode;
        this.transformationString = null;
    }

    /**
     * 获取块大小
     *
     * @return 块大小
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * 设置块大小
     *
     * @param blockSize 块大小
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = Math.max(DEFAULT_BLOCK_SIZE, blockSize);
        this.transformationString = null;
    }

    /**
     * 获取填充方案
     *
     * @return 填充方案
     */
    public PaddingScheme getPaddingScheme() {
        return paddingScheme;
    }

    /**
     * 设置填充方案
     *
     * @param paddingScheme 填充方案
     */
    public void setPaddingScheme(PaddingScheme paddingScheme) {
        this.paddingScheme = paddingScheme;
        this.transformationString = null;
    }

    /**
     * 获取以流加密解密的密码分组模型
     *
     * @return 模型
     */
    public OperationMode getStreamingMode() {
        return streamingMode;
    }

    /**
     * 设置以流加密解密的密码分组模型
     *
     * @param streamingMode 模型
     */
    public void setStreamingMode(OperationMode streamingMode) {
        this.streamingMode = streamingMode;
        this.streamingTransformationString = null;
    }

    /**
     * 获取以流加密解密的设置块大小
     *
     * @return 块大小
     */
    public int getStreamingBlockSize() {
        return streamingBlockSize;
    }

    /**
     * 设置以流加密解密的设置块大小
     *
     * @param streamingBlockSize 块大小
     */
    public void setStreamingBlockSize(int streamingBlockSize) {
        this.streamingBlockSize = Math.max(DEFAULT_STREAMING_BLOCK_SIZE, streamingBlockSize);
        this.streamingTransformationString = null;
    }

    /**
     * 获取以流加密解密的填充方案
     *
     * @return 填充方案
     */
    public PaddingScheme getStreamingPaddingScheme() {
        return streamingPaddingScheme;
    }

    /**
     * 设置以流加密解密的填充方案
     *
     * @param streamingPaddingScheme 填充方案
     */
    public void setStreamingPaddingScheme(PaddingScheme streamingPaddingScheme) {
        this.streamingPaddingScheme = streamingPaddingScheme;
        this.streamingTransformationString = null;
    }

    /**
     * 获取转型字符串
     *
     * @return 字符串
     */
    public String getTransformationString() {
        return transformationString;
    }

    /**
     * 获取以流加密解密的字符串
     *
     * @return 字符串
     */
    public String getStreamingTransformationString() {
        return streamingTransformationString;
    }

}
