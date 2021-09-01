package com.github.dactiv.basic.file.manager.controller;

import com.github.dactiv.basic.file.manager.service.FileManagerService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@Plugin(
        name = "文件管理",
        id = "file",
        icon = "icon-file",
        type = ResourceType.Menu,
        sources = "Console"
)
public class FileManagerController {

    @Autowired
    private FileManagerService fileManagerService;

    /**
     * 桶管理
     *
     * @param bucketName 同名称查询
     *
     * @return 桶列表
     *
     * @throws Exception 获取桶出错时抛出
     */
    @PostMapping("bucket")
    @PreAuthorize("hasAuthority('perms[file_manager:bucket]')")
    @Plugin(name = "桶管理", id="list", sources = "Console",icon = "icon-database", type = ResourceType.Menu)
    public List<Map<String, Object>> bucket(String bucketName) throws Exception {
        return fileManagerService.bucketList(bucketName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param filename 文件名称
     *
     * @return reset 结果集
     *
     * @throws Exception 删除错误时抛出
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[file_manager:delete]')")
    @Plugin(name = "删除文件", parent = "file-manager", sources = "Console", audit = true)
    public RestResult<?> delete(@RequestParam("bucketName") String bucketName,@RequestParam("filename") String filename) throws Exception {
        fileManagerService.delete(bucketName, filename);
        return RestResult.of("删除 [" + filename + "] 成功");
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
    @PostMapping("upload/{bucketName}")
    @PreAuthorize("hasAuthority('perms[file_manager:upload]')")
    @Plugin(name = "上传文件", parent = "file-manager", sources = "Console", audit = true)
    public RestResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, @PathVariable("bucketName") String bucketName) throws Exception{

        Map<String, Object> result = fileManagerService.upload(file, bucketName);

        return RestResult.ofSuccess("上传完成", result);

    }

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param filename 文件名
     *
     * @return 文件流字节
     * @throws Exception 获取失败时抛出
     */
    @PreAuthorize("hasRole('BASIC') or hasAuthority('perms[file_manager:get]')")
    @GetMapping("get/{bucketName}/{filename}")
    @Plugin(name = "获取文件", parent = "file-manager", sources = "System", audit = true)
    public ResponseEntity<byte[]> get(@PathVariable("bucketName") String bucketName, @PathVariable("filename") String filename) throws Exception {
        InputStream is = fileManagerService.get(bucketName, filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(SpringMvcUtils.DEFAULT_ATTACHMENT_NAME, filename);
        return new ResponseEntity<>(IOUtils.toByteArray(is), headers, HttpStatus.OK);
    }

}
