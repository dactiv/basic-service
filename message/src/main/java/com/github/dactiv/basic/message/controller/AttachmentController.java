package com.github.dactiv.basic.message.controller;


import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.message.config.AttachmentConfig;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.FileObject;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import io.minio.ObjectWriteResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

@RefreshScope
@RestController
@RequestMapping("attachment")
@Plugin(
        name = "附件管理",
        id = "attachment",
        parent = "message",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class AttachmentController {

    private final MinioTemplate minioTemplate;

    private final AttachmentConfig attachmentConfig;

    public AttachmentController(MinioTemplate minioTemplate, AttachmentConfig attachmentConfig) {
        this.minioTemplate = minioTemplate;
        this.attachmentConfig = attachmentConfig;
    }

    /**
     * 删除文件
     *
     * @param type     桶类型
     * @param filename 文件名称
     *
     * @return reset 结果集
     *
     * @throws Exception 删除错误时抛出
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[attachment:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除附件", audit = true, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public RestResult<?> delete(@RequestParam("type") String type, @RequestParam("filename") String filename) throws Exception {
        FileObject fileObject = FileObject.of(
                attachmentConfig.getBucketName(type),
                filename
        );
        minioTemplate.deleteObject(fileObject);
        return RestResult.of("删除 [" + filename + "] 成功");
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param type 桶类型
     *
     * @return reset 结果集
     *
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload/{type}")
    @PreAuthorize("hasAuthority('perms[attachment:upload]') and isFullyAuthenticated()")
    @Plugin(name = "上传文件", audit = true, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public RestResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                  @PathVariable("type") String type) throws Exception {
        FileObject fileObject = FileObject.of(
                attachmentConfig.getBucketName(type),
                file.getOriginalFilename()
        );

        ObjectWriteResponse response = minioTemplate.upload(
                fileObject,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        Map<String, Object> result = convertFields(response, response.getClass(), attachmentConfig.getResult().getUploadResultIgnoreFields());

        String url = MessageFormat.format(
                attachmentConfig.getResult().getLinkUri(),
                type,
                fileObject.getObjectName()
        );

        result.put(attachmentConfig.getResult().getLinkParamName(), url);

        return RestResult.ofSuccess("上传完成", result);

    }

    /**
     * 获取文件
     *
     * @param type     桶类型
     * @param filename 文件名
     *
     * @return 文件流字节
     *
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("get/{type}/{filename}")
    @PreAuthorize("hasRole('BASIC') or hasAuthority('perms[attachment:get]')")
    @Plugin(name = "获取文件", sources = ResourceSourceEnum.SYSTEM_SOURCE_VALUE, audit = true)
    public ResponseEntity<byte[]> get(@PathVariable("type") String type, @PathVariable("filename") String filename) throws Exception {
        FileObject fileObject = FileObject.of(
                attachmentConfig.getBucketName(type),
                filename
        );

        InputStream is = minioTemplate.getObject(fileObject);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(SpringMvcUtils.DEFAULT_ATTACHMENT_NAME, filename);
        return new ResponseEntity<>(IOUtils.toByteArray(is), headers, HttpStatus.OK);
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
    private Map<String, Object> convertFields(Object target, Class<?> targetClass, List<String> ignoreFields) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList
                .stream()
                .filter(field -> !ignoreFields.contains(field.getName()))
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
