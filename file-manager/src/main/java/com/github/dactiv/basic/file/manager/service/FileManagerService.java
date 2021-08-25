package com.github.dactiv.basic.file.manager.service;

import com.github.dactiv.basic.file.manager.MinioConfig;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件管理服务
 *
 * @author maurice.chen
 */
@Component
public class FileManagerService {

    public final static String DOWNLOAD_FIELD_KEY = "url";

    @Autowired
    private MinioConfig.MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;

    /**
     * 桶管理
     *
     * @param bucketName 同名称查询
     *
     * @return 桶列表
     *
     * @throws Exception 获取桶出错时抛出
     */
    public List<Map<String, Object>> bucketList(String bucketName) throws Exception {
        return minioClient
                .listBuckets()
                .stream().map(b -> convertFields(b, b.getClass(), "headers"))
                .collect(Collectors.toList());
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param bucketName 放置文件的桶名称
     *
     * @return reset 结果集
     *
     * @throws Exception 上传错误时抛出
     */
    public Map<String, Object> upload(MultipartFile file, String bucketName) throws Exception {

        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        ObjectWriteResponse objectWriteResponse = null;

        try {

            PutObjectArgs args = PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(file.getOriginalFilename())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            objectWriteResponse = minioClient.putObject(args);

            Map<String, Object> result = convertFields(objectWriteResponse, objectWriteResponse.getClass(), "headers");
            result.put(DOWNLOAD_FIELD_KEY, getDownloadUrl(bucketName, file.getOriginalFilename()));

            return result;
        } catch (Exception e) {

            if (Objects.nonNull(objectWriteResponse)) {
                remove(bucketName, file.getOriginalFilename());
            }

            throw e;
        }

    }

    /**
     * 获取下载连接
     *
     * @param bucketName 桶名称
     * @param filename 文件名称
     *
     * @return 下载连接完整路径
     */
    private String getDownloadUrl(String bucketName, String filename) {
        String prefix = StringUtils.appendIfMissing(minioProperties.getDownloadPrefix(), "/");

        return prefix
                + StringUtils.appendIfMissing(StringUtils.removeStart(bucketName, "/"), "/")
                + "objects/download?prefix="+ filename;
    }

    /**
     * 删除文件
     *
     * @param bucketName 同名称
     * @param filename 文件名称
     *
     * @throws Exception 删除错误时抛出
     */
    public void remove(String bucketName, String filename) throws Exception {
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
     * @param filename 文件名称
     *
     * @return 输入流
     *
     * @throws Exception 获取错误时抛出
     */
    public InputStream get(String bucketName, String filename) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filename).build());
    }

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target 目标对象
     * @param targetClass 目标类
     * @param ignoreFields 要忽略的字段名
     *
     * @return map 对象
     */
    private Map<String, Object> convertFields(Object target, Class<?> targetClass, String... ignoreFields) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList
                .stream()
                .filter(field -> !ArrayUtils.contains(ignoreFields, field.getName()))
                .forEach(field -> result.put(field.getName(), getFieldToStringValue(target, field)));

        if (Objects.nonNull(targetClass.getSuperclass())) {
            result.putAll(convertFields(target, targetClass.getSuperclass(), ignoreFields));
        }

        return result;
    }

    /**
     * 获取字段的 toString 值
     *
     * @param target 目标对象
     * @param field 字段
     *
     * @return 值
     */
    private Object getFieldToStringValue(Object target, Field field) {
        Object value = ReflectionUtils.getFieldValue(target, field);

        if (Objects.isNull(value)) {
            return null;
        }

        return String.class.isAssignableFrom(value.getClass()) ? value : value.toString();
    }
}
