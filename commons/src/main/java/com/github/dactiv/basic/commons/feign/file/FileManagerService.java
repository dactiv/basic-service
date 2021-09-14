package com.github.dactiv.basic.commons.feign.file;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.framework.spring.security.authentication.service.feign.AuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件管理服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = Constants.SYS_FILE_MANAGER_NAME, configuration = AuthenticationConfiguration.class)
public interface FileManagerService {

    String DEFAULT_BUCKET_NAME = "bucketName";

    /**
     * 获取文件
     *
     * @param bucketName 同信息
     * @param filename   文件名称
     * @return 字节流
     */
    @GetMapping("get/{" + DEFAULT_BUCKET_NAME + "}/{filename}")
    ResponseEntity<byte[]> get(@PathVariable(DEFAULT_BUCKET_NAME) String bucketName, @PathVariable("filename") String filename);
}
