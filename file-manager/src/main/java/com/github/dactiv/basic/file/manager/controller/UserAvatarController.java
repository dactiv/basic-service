package com.github.dactiv.basic.file.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.file.manager.config.ApplicationConfig;
import com.github.dactiv.basic.file.manager.entity.UserAvatarHistory;
import com.github.dactiv.basic.file.manager.service.FileService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import io.minio.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

/**
 * 用户头像管理
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequestMapping("user/avatar")
@Plugin(
        name = "用户头像管理",
        id = "user-avatar",
        type = ResourceType.Security,
        sources = "Console",
        parent = "file-manager"
)
public class UserAvatarController implements InitializingBean {

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 上传头像
     *
     * @param file 头像
     *
     * @return reset 结果集
     *
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload")
    @Plugin(name = "上传头像", parent = "user-avatar", sources = "System", audit = true)
    @PreAuthorize("hasAuthority('perms[user_avatar:upload]') and isFullyAuthenticated()")
    public RestResult<Map<String, Object>> upload(@CurrentSecurityContext SecurityContext securityContext,
                                                  @RequestParam("file") MultipartFile file) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistory history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(file.getOriginalFilename())) {
            history.getValues().add(file.getOriginalFilename());
        }

        if (history.getValues().size() > applicationConfig.getUserAvatar().getHistoryCount()) {
            history.getValues().remove(0);
        }

        saveUserAvatarHistory(history);

        fileService.upload(file.getOriginalFilename(), file.getInputStream(), file.getSize(), file.getContentType(), history.getBucketName());

        String currentName = MessageFormat.format(applicationConfig.getUserAvatar().getCurrentUseFileToken(), userDetails.getId());
        Map<String, Object> result = fileService.upload(currentName, file.getInputStream(), file.getSize(), file.getContentType(), history.getBucketName());

        return RestResult.ofSuccess("上传新头像完成", result);

    }

    @GetMapping("history")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<UserAvatarHistory> history(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return RestResult.ofSuccess(getUserAvatarHistory(userDetails));
    }

    /**
     * 获取用户历史头像信息
     *
     * @param userDetails 用户明细
     *
     * @return 用户头像历史记录实体
     */
    private UserAvatarHistory getUserAvatarHistory(SecurityUserDetails userDetails) {
        String bucketName = userDetails.getType() + Casts.DEFAULT_DOT_SYMBOL + applicationConfig.getUserAvatar().getBucketName();
        String filename = MessageFormat.format(applicationConfig.getUserAvatar().getHistoryFileToken(), userDetails.getId());

        UserAvatarHistory result = fileService.readJsonValue(bucketName, filename, UserAvatarHistory.class);

        if (Objects.isNull(result)) {

            result = new UserAvatarHistory();

            result.setFilename(filename);
            result.setBucketName(bucketName);
        }

        return result;
    }

    /**
     * 获取用户头像
     *
     * @param type 用户类型
     * @param filename 文件名
     *
     * @return 头像 byte 数组
     *
     * @throws Exception 获取错误时抛出
     */
    @GetMapping("get/{type}/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable("type") String type, @PathVariable("filename") String filename) throws Exception {
        String bucketName = type + Casts.DEFAULT_DOT_SYMBOL + applicationConfig.getUserAvatar().getBucketName();

        InputStream is = fileService.get(bucketName, filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(IOUtils.toByteArray(is), headers, HttpStatus.OK);
    }

    @PostMapping("select")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, Object>> select(@CurrentSecurityContext SecurityContext securityContext,
                                @RequestParam("filename") String filename) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistory history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        String currentName = MessageFormat.format(applicationConfig.getUserAvatar().getCurrentUseFileToken(), userDetails.getId());

        GetObjectResponse response = fileService.get(history.getBucketName(), currentName);
        Headers headers = response.headers();
        Map<String, Object> result = fileService.upload(currentName, response, IOUtils.toByteArray(response).length, headers.get(HttpHeaders.CONTENT_TYPE), history.getBucketName());

        return RestResult.ofSuccess("选择历史头像成功", result);
    }

    @PostMapping("delete")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@CurrentSecurityContext SecurityContext securityContext,
                                @RequestParam("filename") String filename) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistory history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        history.getValues().remove(filename);

        saveUserAvatarHistory(history);

        fileService.delete(history.getBucketName(), filename);

        return RestResult.of("删除历史头像成功");
    }

    private void saveUserAvatarHistory(UserAvatarHistory history) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, history);
        outputStream.flush();

        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        fileService.upload(history.getFilename(), arrayInputStream, bytes.length, MediaType.APPLICATION_JSON_VALUE, history.getBucketName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String s : applicationConfig.getUserAvatar().getUserSources()) {
            String bucketName = s + Casts.DEFAULT_DOT_SYMBOL + applicationConfig.getUserAvatar().getBucketName();
            fileService.makeBucketIfNotExists(bucketName);
        }
    }
}
