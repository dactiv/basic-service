package com.github.dactiv.basic.commons.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Objects;

/**
 * minio 工具类
 *
 * @author maurice.chen
 */
@Slf4j
public class MinioUtils {

    @Setter
    private static MinioClient minioClient;

    @Setter
    private static ObjectMapper objectMapper;

    /**
     * 如果桶名称不存在，创建桶。
     *
     * @param bucketName 桶名称
     *
     * @return 如果桶存在返回 true，否则创建桶后返回 false
     *
     * @throws Exception 创建错误时抛出
     */
    public static boolean makeBucketIfNotExists(String bucketName) throws Exception{
        String name = bucketName.toLowerCase();
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
        }

        return found;
    }

    /**
     * 上传文件
     *
     * @param name        文件名称
     * @param file        文件内容
     * @param contentType 文件类型
     * @param size        文件大小
     * @param bucketName  放置文件的桶名称
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 上传错误时抛出
     */
    public static ObjectWriteResponse upload(String name,
                                             InputStream file,
                                             long size,
                                             String contentType,
                                             String bucketName) throws Exception {

        bucketName = bucketName.toLowerCase();
        makeBucketIfNotExists(bucketName);
        ObjectWriteResponse response = null;

        try {

            PutObjectArgs args = PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(name)
                    .stream(file, size, -1)
                    .contentType(contentType)
                    .build();

            response = minioClient.putObject(args);
        } catch (Exception e) {

            if (Objects.nonNull(response)) {
                delete(bucketName, name);
            }

            throw e;
        }

        return response;

    }

    /**
     * 删除文件
     *
     * @param bucketName 同名称
     * @param filename   文件名称
     *
     * @throws Exception 删除错误时抛出
     */
    public static void delete(String bucketName, String filename) throws Exception {
        bucketName = bucketName.toLowerCase();

        RemoveObjectArgs args = RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .object(filename)
                .build();

        minioClient.removeObject(args);

        Iterable<Result<Item>> iterable = minioClient
                .listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

        if (!iterable.iterator().hasNext()) {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param filename   文件名称
     *
     * @return 输入流
     *
     * @throws Exception 获取错误时抛出
     */
    public static GetObjectResponse get(String bucketName, String filename) throws Exception {
        bucketName = bucketName.toLowerCase();
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filename).build());
    }

    /**
     * 拷贝文件
     *
     * @param fromBucketName 来源桶
     * @param fromFilename 来源文件名
     * @param toBucketName 目的地桶
     *
     * @return minio API 调用响应的 ObjectWriteResponse 对象
     *
     * @throws Exception 拷贝出错时候抛出
     */
    public static ObjectWriteResponse copy(String fromBucketName, String fromFilename, String toBucketName) throws Exception {
        return copy(fromBucketName, fromFilename, toBucketName, null);
    }

    /**
     * 拷贝文件
     *
     * @param fromBucketName 来源桶
     * @param fromFilename 来源文件名
     * @param toBucketName 目的地桶
     * @param newFilename 新文件名称
     *
     * @return minio API 调用响应的 ObjectWriteResponse 对象
     *
     * @throws Exception 拷贝出错时候抛出
     */
    public static ObjectWriteResponse copy(String fromBucketName,
                                    String fromFilename,
                                    String toBucketName,
                                    String newFilename) throws Exception {

        CopyObjectArgs.Builder args = CopyObjectArgs
                .builder()
                .bucket(toBucketName.toLowerCase())
                .source(CopySource.builder().bucket(fromBucketName.toLowerCase()).object(fromFilename).build());

        if (StringUtils.isNotEmpty(newFilename)) {
            args.object(newFilename);
        };

        return minioClient.copyObject(args.build());
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param bucketName 桶名称
     * @param filename 文件名称
     * @param <T> 目标类型
     *
     * @return 目标类型对象
     */
    public static <T> T readJsonValue(String bucketName, String filename, Class<T> targetClass) {
        try {
            InputStream inputStream = get(bucketName, filename);
            return objectMapper.readValue(inputStream, targetClass);
        } catch (Exception e) {
            log.warn("获取桶 [" + bucketName+ "] 的 [" + filename + "] 出现异常", e);
            return null;
        }
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param bucketName 桶名称
     * @param filename 文件名称
     * @param <T> 目标类型
     *
     * @return 目标类型对象
     */
    public static <T> T readJsonValue(String bucketName, String filename, JavaType javaType) {
        try {
            InputStream inputStream = get(bucketName, filename);
            return objectMapper.readValue(inputStream, javaType);
        } catch (Exception e) {
            log.warn("获取桶 [" + bucketName+ "] 的 [" + filename + "] 出现异常", e);
            return null;
        }
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param bucketName 桶名称
     * @param filename 文件名称
     * @param <T> 目标类型
     *
     * @return 目标类型对象
     */
    public static <T> T readJsonValue(String bucketName, String filename, TypeReference<T> typeReference) {
        try {
            InputStream inputStream = get(bucketName, filename);
            return objectMapper.readValue(inputStream, typeReference);
        } catch (Exception e) {
            log.warn("获取桶 [" + bucketName+ "] 的 [" + filename + "] 出现异常", e);
            return null;
        }
    }

}
