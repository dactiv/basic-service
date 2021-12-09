package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.config.dao.DictionaryTypeDao;
import com.github.dactiv.basic.config.entity.DataDictionary;
import com.github.dactiv.basic.config.entity.DictionaryType;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 *
 * tb_dictionary_type 的业务逻辑
 *
 * <p>Table: tb_dictionary_type - 数据字典类型表</p>
 *
 * @see DictionaryType
 *
 * @author maurice.chen
 *
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DictionaryTypeService extends BasicService<DictionaryTypeDao, DictionaryType> {

    private final DataDictionaryService dataDictionaryService;

    public DictionaryTypeService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    /**
     * 获取数据字典
     *
     * @param code 代码
     *
     * @return 数据字典
     */
    public DictionaryType getByCode(String code) {
        return lambdaQuery().eq(DictionaryType::getCode, code).one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     *
     * @return 数据字典集合
     */
    public List<DictionaryType> getByParentId(Integer parentId) {
        return lambdaQuery().eq(DictionaryType::getParentId, parentId).list();
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = 0;

        Wrapper<DictionaryType> wrapper = Wrappers
                .<DictionaryType>lambdaQuery()
                .select(DictionaryType::getId)
                .in(DictionaryType::getParentId, ids);

        List<Integer> subIds = findObjects(wrapper, Integer.class);

        List<Integer> dataDictionaryIds = dataDictionaryService.findIdByTypeId(subIds);

        if (CollectionUtils.isNotEmpty(dataDictionaryIds)) {
            result += dataDictionaryService.deleteById(dataDictionaryIds);
        }

        if (CollectionUtils.isNotEmpty(subIds)) {
            result += deleteById(subIds, errorThrow);
        }

        result += super.deleteById(ids, errorThrow);

        return result;
    }
}
