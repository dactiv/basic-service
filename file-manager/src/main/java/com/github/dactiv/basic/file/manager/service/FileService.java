package com.github.dactiv.basic.file.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.commons.feign.file.FileManagerService;
import com.github.dactiv.basic.commons.utils.MinioUtils;
import com.github.dactiv.basic.file.manager.config.ApplicationConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class FileService {

    public static final String[] DEFAULT_IGNORE_FIELDS = {"headers"};

    public final static String URL_FIELD_KEY = "url";

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 自动关删除桶里的过期文件
     *
     * @throws Exception 获取桶信息出错时抛出
     */
    @NacosCronScheduled(cron = "${spring.minio.auto-delete.cron:0 1 * * * ?}", name = "自动清除过期文件服务")
    public void autoDelete() throws Exception {

        Set<String> bucketNameList = applicationConfig.getAutoDelete().getExpiration().keySet();

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

        TimeProperties time = applicationConfig
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
                    MinioUtils.delete(bucket.name(), item.objectName());
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
     * @param name        文件名称
     * @param file        文件内容
     * @param contentType 文件类型
     * @param size        文件大小
     * @param bucketName  放置文件的桶名称
     *
     * @return reset 结果集
     *
     * @throws Exception 上传错误时抛出
     */
    public Map<String, Object> upload(String name,
                                      InputStream file,
                                      long size,
                                      String contentType,
                                      String bucketName) throws Exception {

        ObjectWriteResponse response = MinioUtils.upload(name, file, size, contentType, bucketName);
        Map<String, Object> result = convertFields(response, response.getClass(), DEFAULT_IGNORE_FIELDS);

        String url = Casts.setUrlPathVariableValue(
                applicationConfig.getDownloadUrl(),
                Map.of(
                        FileManagerService.DEFAULT_BUCKET_NAME, bucketName,
                        FileManagerService.DEFAULT_FILENAME, name
                )
        );

        result.put(URL_FIELD_KEY, url);

        return result;

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
