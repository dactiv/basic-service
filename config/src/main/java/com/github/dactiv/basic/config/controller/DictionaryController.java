package com.github.dactiv.basic.config.controller;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.spring.web.RestResult;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.basic.config.dao.entity.DataDictionary;
import com.github.dactiv.basic.config.dao.entity.DictionaryType;
import com.github.dactiv.basic.config.service.DictionaryService;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        source = ResourceSource.Console
)
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    // ----------------------------------------------- 数据字典管理 ----------------------------------------------- //

    /**
     * 获取数据字典分页信息
     *
     * @param pageRequest 分页信息
     * @param filter      过滤条件
     * @return 分页实体
     */
    @PostMapping("getDataDictionaryPage")
    @PreAuthorize("hasAuthority('perms[data-dictionary:page]')")
    @Plugin(name = "获取数据字典分页", source = ResourceSource.Console)
    public Page<DataDictionary> getDataDictionaryPage(PageRequest pageRequest, @RequestParam Map<String, Object> filter) {
        return dictionaryService.findDataDictionariesPage(pageRequest, filter);
    }

    /**
     * 获取所有数据字典
     *
     * @param filter    过滤条件
     * @param mergeTree 是否合并树形，true 是，否则 false
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionary")
    @Plugin(name = "查询全部", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[data-dictionary:find]')")
    public List<DataDictionary> findDataDictionary(@RequestParam Map<String, Object> filter,
                                                   @RequestParam(required = false) boolean mergeTree) {
        List<DataDictionary> dataDictionaries = dictionaryService.findDataDictionaries(filter);

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
     * @return true 是，否则 false
     */
    @GetMapping("isDataDictionaryCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断数据字典唯一识别值是否唯一", source = ResourceSource.Console)
    public boolean isDataDictionaryCodeUnique(@RequestParam String code) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("codeEq", code);
        return dictionaryService.findDataDictionaries(filter).size() > 0;
    }

    /**
     * 获取数据字典
     *
     * @param id 数据字典 ID
     * @return 数据字典实体
     */
    @GetMapping("getDataDictionary")
    @PreAuthorize("hasAuthority('perms[data-dictionary:get]')")
    @Plugin(name = "获取数据字典实体信息", source = ResourceSource.Console)
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
    @Plugin(name = "保存数据字典实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> saveDataDictionary(@Valid DataDictionary entity) {
        dictionaryService.saveDataDictionary(entity);
        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDataDictionary")
    @PreAuthorize("hasAuthority('perms[data-dictionary:delete]')")
    @Plugin(name = "删除数据字典实体", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> deleteDataDictionary(@RequestParam List<Integer> ids) {
        dictionaryService.deleteDataDictionaries(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }

    // ----------------------------------------------- 字典类型管理 ----------------------------------------------- //

    /**
     * 获取所有字典类型
     *
     * @param filter    过滤条件
     * @param mergeTree 是否合并树形，true 是，否则 false
     * @return 字典类型集合
     */
    @PostMapping("findDictionaryType")
    @Plugin(name = "查询全部", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[dictionary-type:find]')")
    public List<DictionaryType> findDictionaryType(@RequestParam Map<String, Object> filter,
                                                   @RequestParam(required = false) boolean mergeTree) {
        List<DictionaryType> dictionaryTypes = dictionaryService.findDictionaryTypes(filter);

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
     * @return 字典类型实体
     */
    @GetMapping("getDictionaryType")
    @Plugin(name = "获取信息", source = ResourceSource.Console)
    @PreAuthorize("hasAuthority('perms[dictionary-type:get]')")
    public DictionaryType getDictionaryType(@RequestParam Integer id) {
        return dictionaryService.getDictionaryType(id);
    }

    /**
     * 判断字典类型唯一识别值是否唯一
     *
     * @param code 唯一识别值
     * @return true 是，否则 false
     */
    @GetMapping("isDictionaryTypeCodeUnique")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "判断字类型典唯一识别值是否唯一", source = ResourceSource.Console)
    public boolean isDictionaryTypeCodeUnique(@RequestParam String code) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("codeEq", code);
        return dictionaryService.findDataDictionaries(filter).size() > 0;
    }

    /**
     * 保存数据字典类型
     *
     * @param entity 数据字典类型实体
     */
    @PostMapping("saveDictionaryType")
    @PreAuthorize("hasAuthority('perms[dictionary-type:save]')")
    @Plugin(name = "保存", source = ResourceSource.Console, audit = true)
    public RestResult.Result<Map<String, Object>> saveDictionaryType(@Valid DictionaryType entity) {
        dictionaryService.saveDictionaryType(entity);

        return RestResult.build("保存成功", entity.idEntityToMap());
    }

    /**
     * 删除字典类型类型
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDictionaryType")
    @PreAuthorize("hasAuthority('perms[dictionary-type:delete]')")
    @Plugin(name = "删除", source = ResourceSource.Console, audit = true)
    public RestResult.Result<?> deleteDictionaryType(@RequestParam List<Integer> ids) {
        dictionaryService.deleteDictionaryTypes(ids);
        return RestResult.build("删除" + ids.size() + "条记录成功");
    }


}
