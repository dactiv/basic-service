package com.github.dactiv.basic.config.controller;

import com.github.dactiv.basic.config.entity.ConfigAccessCrypto;
import com.github.dactiv.basic.config.service.AccessCryptoService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

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
        icon = "icon-crypto-currency-bitcoin-imac",
        type = ResourceType.Menu,
        sources = "Console"
)
public class AccessCryptoController {

    @Autowired
    private AccessCryptoService accessCryptoService;

    @Autowired
    private MybatisPlusQueryGenerator<ConfigAccessCrypto> queryGenerator;

    /**
     * 获取访问加解密分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[access_crypto:page]')")
    @Plugin(name = "获取访问加解密分页", sources = "Console")
    public Page<ConfigAccessCrypto> page(PageRequest pageRequest, HttpServletRequest request) {
        return accessCryptoService.findAccessCryptoPage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取访问加解密
     *
     * @param id 访问加解密主键 id
     *
     * @return 访问加解密实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[access_crypto:get]')")
    @Plugin(name = "获取访问加解密实体信息", sources = "Console")
    public AccessCrypto get(@RequestParam Integer id) {
        return accessCryptoService.getAccessCrypto(id);
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    @PostMapping("save")
    @Plugin(name = "保存访问加解密实体", sources = "Console", audit = true)
    @PreAuthorize("hasAuthority('perms[access_crypto:save]') and isFullyAuthenticated()")
    @Idempotent(
            key = "idempotent:config:access:crypto:save:[#securityContext.authentication.details.id]",
            ignore = "securityContext"
    )
    public RestResult<Integer> save(@RequestBody @Valid ConfigAccessCrypto entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        accessCryptoService.saveAccessCrypto(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除访问加解密
     *
     * @param ids 主键值集合
     */
    @PostMapping("delete")
    @Plugin(name = "删除访问加解密实体", sources = "Console", audit = true)
    @PreAuthorize("hasAuthority('perms[access_crypto:delete]') and isFullyAuthenticated()")
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        accessCryptoService.deleteAccessCrypto(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
