package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.config.dao.DataDictionaryDao;
import com.github.dactiv.basic.config.domain.entity.DataDictionaryEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * tb_data_dictionary 的业务逻辑
 *
 * <p>Table: tb_data_dictionary - 数据字典表</p>
 *
 * @author maurice.chen
 * @see DataDictionaryEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataDictionaryService extends BasicService<DataDictionaryDao, DataDictionaryEntity> {

    /**
     * 获取数据字典
     *
     * @param code 代码
     *
     * @return 数据字典
     */
    public DataDictionaryEntity getByCode(String code) {
        return lambdaQuery().eq(DataDictionaryEntity::getCode, code).one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     *
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByParentId(Integer parentId) {
        return lambdaQuery().eq(DataDictionaryEntity::getParentId, parentId).list();
    }

    /**
     * 获取数据字典集合
     *
     * @param typeId 字典类型 id
     *
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByTypeId(Integer typeId) {
        return lambdaQuery().eq(DataDictionaryEntity::getTypeId, typeId).list();
    }

    /**
     * 获取数据字典集合
     *
     * @param typeId 字典类型 id 集合
     *
     * @return 数据字典集合
     */
    public List<Integer> findIdByTypeId(Collection<Integer> typeId) {
        Wrapper<DataDictionaryEntity> wrapper = Wrappers
                .<DataDictionaryEntity>lambdaQuery()
                .select(DataDictionaryEntity::getId)
                .in(DataDictionaryEntity::getTypeId, typeId);

        return findObjects(wrapper, Integer.class);
    }
}
