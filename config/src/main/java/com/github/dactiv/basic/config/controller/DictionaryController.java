package com.github.dactiv.basic.config.controller;

import com.github.dactiv.basic.config.dao.entity.DataDictionary;
import com.github.dactiv.basic.config.dao.entity.DictionaryType;
import com.github.dactiv.basic.config.service.DictionaryService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 数据字典管理控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("dictionary")
@Plugin(
        name = "数据字典管理",
        id = "dictionary",
        parent = "config",
        type = ResourceType.Menu,
        sources = "Console"
)
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private MybatisPlusQueryGenerator<?> mybatisPlusQueryGenerator;

    // ----------------------------------------------- 数据字典管理 ----------------------------------------------- //

    /**
     * 获取数据字典分页信息
     *
     * @param pageable 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     */
    @PostMapping("getDataDictionaryPage")
    @PreAuthorize("hasAuthority('perms[data-dictionary:page]')")
    @Plugin(name = "获取数据字典分页", sources = "Console")
    public Page<DataDictionary> getDataDictionaryPage(Pageable pageable, HttpServletRequest request) {
        return dictionaryService.findDataDictionariesPage(
                pageable,
                mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 获取所有数据字典
     *
     * @param request   http servlet request
     * @param mergeTree 是否合并树形，true 是，否则 false
     *
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionary")
    @Plugin(name = "查询全部", sources = "Console")
    @PreAuthorize("hasAuthority('perms[data-dictionary:find]')")
    public List<DataDictionary> findDataDictionary(HttpServletRequest request,
                                                   @RequestParam(required = false) boolean mergeTree) {

        List<DataDictionary> dataDictionaries = dictionaryService.findDataDictionaries(
                mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request)
        );

        if (mergeTree) {
            return TreeUtils.buildGenericTree(dataDictionaries);
        } else {
            return dataDictionaries;
        }
    }

    /**
     * 判断数据字典唯一识别值是否唯一
     *
     * @param code 唯一识别值
     *
     * @return true 是，否则 false
     */
    @GetMapping("isDataDictionaryCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断数据字典唯一识别值是否唯一", sources = "Console")
    public boolean isDataDictionaryCodeUnique(@RequestParam String code) {
        return Objects.isNull(dictionaryService.getDataDictionaryByCode(code));
    }

    /**
     * 获取数据字典
     *
     * @param id 数据字典 ID
     *
     * @return 数据字典实体
     */
    @GetMapping("getDataDictionary")
    @PreAuthorize("hasAuthority('perms[data-dictionary:get]')")
    @Plugin(name = "获取数据字典实体信息", sources = "Console")
    public DataDictionary getDataDictionary(@RequestParam Integer id) {
        return dictionaryService.getDataDictionary(id);
    }

    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    @PostMapping("saveDataDictionary")
    @PreAuthorize("hasAuthority('perms[data-dictionary:save]')")
    @Plugin(name = "保存数据字典实体", sources = "Console", audit = true)
    public RestResult<Integer> saveDataDictionary(@Valid DataDictionary entity) {
        dictionaryService.saveDataDictionary(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDataDictionary")
    @PreAuthorize("hasAuthority('perms[data-dictionary:delete]')")
    @Plugin(name = "删除数据字典实体", sources = "Console", audit = true)
    public RestResult.Result<?> deleteDataDictionary(@RequestParam List<Integer> ids) {
        dictionaryService.deleteDataDictionaries(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

    // ----------------------------------------------- 字典类型管理 ----------------------------------------------- //

    /**
     * 获取所有字典类型
     *
     * @param request   http servlet request
     * @param mergeTree 是否合并树形，true 是，否则 false
     *
     * @return 字典类型集合
     */
    @PostMapping("findDictionaryType")
    @Plugin(name = "查询全部", sources = "Console")
    @PreAuthorize("hasAuthority('perms[dictionary-type:find]')")
    public List<DictionaryType> findDictionaryType(HttpServletRequest request,
                                                   @RequestParam(required = false) boolean mergeTree) {

        List<DictionaryType> dictionaryTypes = dictionaryService.findDictionaryTypes(
                mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request)
        );

        if (mergeTree) {
            return TreeUtils.buildGenericTree(dictionaryTypes);
        } else {
            return dictionaryTypes;
        }
    }

    /**
     * 获取字典类型实体
     *
     * @param id 主键 ID
     *
     * @return 字典类型实体
     */
    @GetMapping("getDictionaryType")
    @Plugin(name = "获取信息", sources = "Console")
    @PreAuthorize("hasAuthority('perms[dictionary-type:get]')")
    public DictionaryType getDictionaryType(@RequestParam Integer id) {
        return dictionaryService.getDictionaryType(id);
    }

    /**
     * 判断字典类型唯一识别值是否唯一
     *
     * @param code 唯一识别值
     *
     * @return true 是，否则 false
     */
    @GetMapping("isDictionaryTypeCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断字类型典唯一识别值是否唯一", sources = "Console")
    public boolean isDictionaryTypeCodeUnique(@RequestParam String code) {

        return Objects.isNull(dictionaryService.getDataDictionaryByCode(code));
    }

    /**
     * 保存数据字典类型
     *
     * @param entity 数据字典类型实体
     */
    @PostMapping("saveDictionaryType")
    @PreAuthorize("hasAuthority('perms[dictionary-type:save]')")
    @Plugin(name = "保存", sources = "Console", audit = true)
    public RestResult.Result<Integer> saveDictionaryType(@Valid DictionaryType entity) {
        dictionaryService.saveDictionaryType(entity);
        return RestResult.build("保存成功", entity.getId());
    }

    /**
     * 删除字典类型类型
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDictionaryType")
    @PreAuthorize("hasAuthority('perms[dictionary-type:delete]')")
    @Plugin(name = "删除", sources = "Console", audit = true)
    public RestResult.Result<?> deleteDictionaryType(@RequestParam List<Integer> ids) {
        dictionaryService.deleteDictionaryTypes(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }


}
