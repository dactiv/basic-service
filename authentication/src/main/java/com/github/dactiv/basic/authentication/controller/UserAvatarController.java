package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.basic.authentication.domain.entity.UserAvatarHistoryEntity;
import com.github.dactiv.basic.authentication.service.ConsoleUserService;
import com.github.dactiv.basic.authentication.service.MemberUserService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
        parent = "file-manager"
)
public class UserAvatarController implements InitializingBean {

    private final ApplicationConfig applicationConfig;

    private final MinioTemplate minioTemplate;

    private final ConsoleUserService consoleUserService;

    private final MemberUserService memberUserService;

    public UserAvatarController(ApplicationConfig applicationConfig,
                                MinioTemplate minioTemplate,
                                ConsoleUserService consoleUserService,
                                MemberUserService memberUserService) {
        this.applicationConfig = applicationConfig;
        this.minioTemplate = minioTemplate;

        this.consoleUserService = consoleUserService;
        this.memberUserService = memberUserService;
    }

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
    @PreAuthorize("hasAuthority('perms[user_avatar:upload]') and isFullyAuthenticated()")
    @Plugin(name = "上传头像", parent = "user-avatar", sources = ResourceSourceEnum.SYSTEM_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:authentication:user:avatar:upload:[#securityContext.authentication.details.id]")
    public RestResult<Map<String, Object>> upload(@CurrentSecurityContext SecurityContext securityContext,
                                                  @RequestParam MultipartFile file) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistoryEntity history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(file.getOriginalFilename())) {
            history.getValues().add(file.getOriginalFilename());
        }

        if (history.getValues().size() > applicationConfig.getUserAvatar().getHistoryCount()) {
            history.getValues().remove(0);
        }

        history.setCurrentAvatarFilename(file.getOriginalFilename());
        minioTemplate.writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);
        minioTemplate.upload(
                FileObject.of(history.getBucketName(), file.getOriginalFilename()),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        String currentName = getCurrentAvatarFilename(userDetails.getId());
        minioTemplate.upload(
                FileObject.of(history.getBucketName(), currentName),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        return RestResult.of("上传新头像完成");

    }

    /**
     * 获取历史头像
     *
     * @param securityContext 安全上下文
     *
     * @return 用户头像历史记录实体
     */
    @GetMapping("history")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<UserAvatarHistoryEntity> history(@CurrentSecurityContext SecurityContext securityContext) {
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
    private UserAvatarHistoryEntity getUserAvatarHistory(SecurityUserDetails userDetails) {
        String bucketName = userDetails.getType() +
                Casts.DEFAULT_DOT_SYMBOL +
                applicationConfig.getUserAvatar().getBucketName();

        String token = applicationConfig.getUserAvatar().getHistoryFileToken();
        String filename = MessageFormat.format(token, userDetails.getId());

        FileObject fileObject = FileObject.of(bucketName, filename);
        UserAvatarHistoryEntity result = minioTemplate.readJsonValue(fileObject, UserAvatarHistoryEntity.class);

        if (Objects.isNull(result)) {

            result = new UserAvatarHistoryEntity();

            result.setHistoryFilename(filename);
            result.setBucketName(bucketName);
        }

        return result;
    }

    /**
     * 获取用户头像
     *
     * @param type     用户类型
     * @param filename 文件名
     *
     * @return 头像 byte 数组
     *
     * @throws Exception 获取错误时抛出
     */
    @GetMapping("get/{type}/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable("type") String type,
                                      @PathVariable("filename") String filename) throws Exception {

        String bucketName = type + Casts.DEFAULT_DOT_SYMBOL + applicationConfig.getUserAvatar().getBucketName();

        InputStream is;

        try {
            is = minioTemplate.getObject(FileObject.of(bucketName, filename));
        } catch (Exception e) {
            String token = StringUtils.substringBefore(applicationConfig.getUserAvatar().getCurrentUseFileToken(), Casts.PATH_VARIABLE_SYMBOL_START);
            Integer id = NumberUtils.toInt(StringUtils.replace(filename, token, StringUtils.EMPTY));

            SystemUserEntity user;

            if (ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(type)) {
                user = consoleUserService.get(id);
            } else if (ResourceSourceEnum.USER_CENTER_SOURCE_VALUE.equals(type)) {
                user = memberUserService.get(id);
            } else {
                throw new SystemException("找不到类型为 [" + type + "] 的头像获取内容");
            }
            is = applicationConfig.getUserAvatar().getDefaultAvatarPath(user.getGender(), id);
        }

        return new ResponseEntity<>(IOUtils.toByteArray(is), new HttpHeaders(), HttpStatus.OK);
    }

    /**
     * 选择历史头像
     *
     * @param securityContext 安全上下文
     * @param filename        历史头像文件名称
     *
     * @return rest 结果集
     *
     * @throws Exception 拷贝文件错误时抛出
     */
    @PostMapping("select")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> select(@CurrentSecurityContext SecurityContext securityContext,
                                @RequestParam String filename) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistoryEntity history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        String currentName = getCurrentAvatarFilename(userDetails.getId());
        minioTemplate.copyObject(
                FileObject.of(history.getBucketName(), filename),
                FileObject.of(history.getBucketName(), currentName)
        );

        history.setCurrentAvatarFilename(filename);

        minioTemplate.writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        return RestResult.of("更换头像成功");
    }

    /**
     * 获取当前头像文件名称
     *
     * @param suffix 后缀
     *
     * @return 当前头像文件名称
     */
    private String getCurrentAvatarFilename(Object suffix) {
        String token = applicationConfig.getUserAvatar().getCurrentUseFileToken();
        return MessageFormat.format(token, suffix);
    }

    /**
     * 删除历史头像
     *
     * @param securityContext 安全上下文
     * @param filename        要删除的文件名称
     *
     * @return rest 结果集
     *
     * @throws Exception 删除错误时候抛出
     */
    @PostMapping("delete")
    @PreAuthorize("isFullyAuthenticated()")
    @Idempotent(key = "idempotent:authentication:user:avatar:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@CurrentSecurityContext SecurityContext securityContext,
                                @RequestParam String filename) throws Exception {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        UserAvatarHistoryEntity history = getUserAvatarHistory(userDetails);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        history.getValues().remove(filename);

        if (StringUtils.equals(filename, history.getCurrentAvatarFilename())) {
            history.setCurrentAvatarFilename("");
        }

        minioTemplate.writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        minioTemplate.deleteObject(FileObject.of(history.getBucketName(), filename));

        return RestResult.of("删除历史头像成功");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String s : applicationConfig.getUserAvatar().getUserSources()) {
            String name = ResourceSourceEnum.getMinioBucket(s);
            String bucketName = applicationConfig.getUserAvatar().getBucketName() + Casts.DEFAULT_DOT_SYMBOL + name;
            minioTemplate.makeBucketIfNotExists(Bucket.of(bucketName));
        }
    }
}
