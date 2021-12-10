package com.github.dactiv.basic.config.controller;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.config.domain.entity.AccessCryptoEntity;
import com.github.dactiv.basic.config.service.AccessCryptoService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
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
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class AccessCryptoController {

    private final AccessCryptoService accessCryptoService;

    private final MybatisPlusQueryGenerator<AccessCryptoEntity> queryGenerator;

    public AccessCryptoController(AccessCryptoService accessCryptoService,
                                  MybatisPlusQueryGenerator<AccessCryptoEntity> queryGenerator) {
        this.accessCryptoService = accessCryptoService;
        this.queryGenerator = queryGenerator;
    }

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
    @Plugin(name = "获取访问加解密分页", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<AccessCryptoEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return accessCryptoService.findPage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
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
    @Plugin(name = "获取访问加解密实体信息", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public AccessCryptoEntity get(@RequestParam Integer id) {
        return accessCryptoService.get(id);
    }

    /**
     * 获取所有通讯加解密
     *
     * @return 通讯加解密 集合
     */
    @GetMapping("getAll")
    @PreAuthorize("hasRole('BASIC')")
    public List<AccessCryptoEntity> getAll(){
        return accessCryptoService.getAll();
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[access_crypto:save]') and isFullyAuthenticated()")
    @Plugin(name = "保存访问加解密实体", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:config:access:crypto:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> save(@RequestBody @Valid AccessCryptoEntity entity,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        accessCryptoService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除访问加解密
     *
     * @param ids 主键值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[access_crypto:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除访问加解密实体", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    @Idempotent(key = "idempotent:config:access:crypto:save:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        accessCryptoService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
