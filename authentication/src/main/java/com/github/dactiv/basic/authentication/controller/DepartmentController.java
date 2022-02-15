package com.github.dactiv.basic.authentication.controller;

import com.github.dactiv.basic.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.basic.authentication.service.DepartmentService;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 *
 * tb_department 的控制器
 *
 * <p>Table: tb_department - 部门表</p>
 *
 * @see DepartmentEntity
 *
 * @author maurice.chen
 *
 * @since 2022-02-09 06:47:53
 */
@RestController
@RequestMapping("department")
@Plugin(
    name = "部门管理",
    id = "department",
    parent = "organization",
    icon = "icon-department",
    type = ResourceType.Menu,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MybatisPlusQueryGenerator<?> queryGenerator;

    /**
     * 获取 table: tb_department 信息
     *
     * @param request  http servlet request
     *
     * @return tb_department 实体集合
     *
     * @see DepartmentEntity
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[department:find]')")
    @Plugin(name = "获取全部", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public List<DepartmentEntity> find(HttpServletRequest request,
                                       @RequestParam(required = false) boolean mergeTree) {
        List<DepartmentEntity> result = departmentService.find(queryGenerator.getQueryWrapperByHttpRequest(request));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(result);
        } else {
            return result;
        }
    }

    /**
     * 获取 table: tb_department 分页信息
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see DepartmentEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[department:page]')")
    @Plugin(name = "获取分页", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<DepartmentEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return departmentService.findPage(
                pageRequest,
                queryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取 table: tb_department 实体
     *
     * @param id 主键 ID
     *
     * @return tb_department 实体
     *
     * @see DepartmentEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[department:get]')")
    @Plugin(name = "获取实体", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public DepartmentEntity get(@RequestParam Integer id) {
        return departmentService.get(id);
    }

    /**
     * 保存 table: tb_department 实体
     *
     * @param entity tb_department 实体
     *
     * @see DepartmentEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[department:save]')")
    @Plugin(name = "保存实体", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Integer> save(@Valid @RequestBody DepartmentEntity entity) {
        departmentService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_department 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see DepartmentEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[department:delete]')")
    @Plugin(name = "删除实体", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        departmentService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断邮件是否唯一
     *
     * @param name 电子邮件
     *
     * @return true 是，否则 false
     */
    @GetMapping("isNameUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断邮件是否唯一", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public boolean isEmailUnique(@RequestParam String name) {
        return !departmentService
                .lambdaQuery()
                .select(DepartmentEntity::getId)
                .eq(DepartmentEntity::getName, name)
                .exists();
    }

}
