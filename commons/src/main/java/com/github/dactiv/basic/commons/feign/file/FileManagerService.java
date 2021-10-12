package com.github.dactiv.basic.commons.feign.file;

import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件管理服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = Constants.SYS_FILE_MANAGER_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface FileManagerService {

    String DEFAULT_BUCKET_NAME = "bucketName";

    String DEFAULT_FILENAME = "filename";

    /**
     * 获取文件
     *
     * @param bucketName 同信息
     * @param filename   文件名称
     *
     * @return 字节流
     */
    @GetMapping("get/{" + DEFAULT_BUCKET_NAME + "}/{" + DEFAULT_FILENAME + "}")
    ResponseEntity<byte[]> get(@PathVariable(DEFAULT_BUCKET_NAME) String bucketName,
                               @PathVariable(DEFAULT_FILENAME) String filename);
}
