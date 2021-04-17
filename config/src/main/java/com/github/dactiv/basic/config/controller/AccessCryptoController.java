package com.github.dactiv.basic.config.controller;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.spring.web.RestResult;
import com.github.dactiv.basic.config.service.AccessCryptoService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 访问加解密控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("access/crypto")
@Plugin(
        name = "访问加解密",
        id = "access_crypto",
        parent = "config",
        type = ResourceType.Menu,
        source = ResourceSource.Console
)
public class AccessCryptoController {

    @Autowired
    private AccessCryptoService accessCryptoService;

    /**
     * 获取访问加解密分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[access_crypto:page]')")
    @Plugin(name = "获取访问加解密分页", source = ResourceSource.Console)
    public Page<AccessCrypto> page(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return accessCryptoService.findAccessCryptoPage(pageRequest, filter);
    }

    /**
     * 获取访问加解密
     *
     * @param id 访问加解密主键 id
     * @return 访问加解密实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[access_crypto:get]')")
    @Plugin(name = "获取访问加解密实体信息", source = ResourceSource.Console)
    public AccessCrypto get(@RequestParam Integer id) {
        return accessCryptoService.getAccessCrypto(id);
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[access_crypto:save]')")
    @Plugin(name = "保存访问加解密实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> save(@RequestBody @Valid AccessCrypto entity) {
        accessCryptoService.saveAccessCrypto(entity);
        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除访问加解密
     *
     * @param ids 主键值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[access_crypto:delete]')")
    @Plugin(name = "删除访问加解密实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> delete(@RequestParam List<Integer> ids) {
        accessCryptoService.deleteAccessCrypto(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }
}
