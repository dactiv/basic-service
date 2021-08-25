package com.github.dactiv.basic.file.manager.controller;

import com.github.dactiv.basic.file.manager.service.FileManagerService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
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
    @PostMapping("bucketList")
    @PreAuthorize("hasAuthority('perms[file_manager:bucket_list]')")
    @Plugin(name = "桶管理", id="list", sources = "Console", parent = "file-manager", icon = "icon-database", type = ResourceType.Menu)
    public List<Map<String, Object>> bucketList(String bucketName) throws Exception {
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
    public RestResult<?> remove(@RequestParam("bucketName") String bucketName,@RequestParam("filename") String filename) throws Exception {
        fileManagerService.remove(bucketName, filename);

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
    @PostMapping("upload")
    @PreAuthorize("hasAuthority('perms[file_manager:upload]')")
    @Plugin(name = "上传文件", parent = "file-manager", sources = "Console", audit = true)
    public RestResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, @RequestParam("bucketName") String bucketName) throws Exception{

        Map<String, Object> result = fileManagerService.upload(file, bucketName);

        return RestResult.ofSuccess("上传完成", result);

    }

}
