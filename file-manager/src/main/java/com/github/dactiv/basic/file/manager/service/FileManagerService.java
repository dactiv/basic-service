package com.github.dactiv.basic.file.manager.service;

import com.github.dactiv.basic.file.manager.MinioConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件管理服务
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class FileManagerService {

    public final static String URL_FIELD_KEY = "url";

    @Autowired
    private MinioConfig.MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;

    /**
     * 自动关删除桶里的过期文件
     *
     * @throws Exception 获取桶信息出错时抛出
     */
    @NacosCronScheduled(cron = "${spring.minio.auto-delete.cron:0 1 * * * ?}", name = "自动清除过期文件服务")
    public void autoDelete() throws Exception {

        Set<String> bucketNameList = minioProperties.getAutoDelete().getExpiration().keySet();

        if (log.isDebugEnabled()) {
            log.debug("开始自动删除桶的过期对象");
        }

        List<Bucket> bucketList = minioClient
                .listBuckets()
                .stream()
                .filter(b -> bucketNameList.contains(b.name()))
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("需要自动删除桶的数据为:" + bucketList);
        }

        for (Bucket bucket : bucketList) {
            try {
                deleteExpiredFiles(bucket);
            } catch (Exception e) {
                log.error("删除桶 [" + bucket.name() + "] 的过期对象失败", e);
            }
        }

    }

    /**
     * 删除过期文件
     *
     * @param bucket 桶信息
     */
    public void deleteExpiredFiles(Bucket bucket) {

        Iterable<Result<Item>> iterable = minioClient
                .listObjects(ListObjectsArgs.builder().bucket(bucket.name()).build());

        TimeProperties time = minioProperties
                .getAutoDelete()
                .getExpiration()
                .get(bucket.name());

        if (Objects.isNull(time)) {
            throw new SystemException("找不到 [" + bucket.name() + "] 桶的自动删除时间配置。");
        }

        for (Result<Item> result : iterable) {

            try {

                Item item = result.get();

                if (item.isDeleteMarker()) {
                    continue;
                }

                LocalDateTime expirationTime = item
                        .lastModified()
                        .toLocalDateTime()
                        .plus(time.getValue(), time.getUnit().toChronoUnit());

                if (LocalDateTime.now().isAfter(expirationTime)) {
                    delete(bucket.name(), item.objectName());
                    log.info("删除桶的 [" + bucket.name() + "] 的 [" + item.objectName() + "] 对象");
                }

            } catch (Exception e) {
                log.error("获取对象失败", e);
            }

        }
    }

    /**
     * 上传文件
     *
     * @param file       文件
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

            Map<String, String> variableValue = new LinkedHashMap<>(2);

            variableValue.put("bucketName", bucketName);
            variableValue.put("filename", file.getOriginalFilename());

            String url = Casts.setUrlPathVariableValue(minioProperties.getDownloadUrl(), variableValue);

            result.put(URL_FIELD_KEY, url);

            return result;
        } catch (Exception e) {

            if (Objects.nonNull(objectWriteResponse)) {
                delete(bucketName, file.getOriginalFilename());
            }

            throw e;
        }

    }

    /**
     * 删除文件
     *
     * @param bucketName 同名称
     * @param filename   文件名称
     *
     * @throws Exception 删除错误时抛出
     */
    public void delete(String bucketName, String filename) throws Exception {
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
    public InputStream get(String bucketName, String filename) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filename).build());
    }

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target       目标对象
     * @param targetClass  目标类
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
     * @param field  字段
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
