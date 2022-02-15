package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.config.config.ApplicationConfig;
import com.github.dactiv.basic.config.domain.entity.DataDictionaryEntity;
import com.github.dactiv.basic.config.domain.entity.DictionaryTypeEntity;
import com.github.dactiv.framework.commons.exception.ServiceException;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 字典管理
 *
 * @author maurice
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DictionaryService {

    @Getter
    private final DataDictionaryService dataDictionaryService;

    @Getter
    private final DictionaryTypeService dictionaryTypeService;

    private final ApplicationConfig config;

    public DictionaryService(DataDictionaryService dataDictionaryService,
                             DictionaryTypeService dictionaryTypeService,
                             ApplicationConfig config) {
        this.dataDictionaryService = dataDictionaryService;
        this.dictionaryTypeService = dictionaryTypeService;
        this.config = config;
    }

    // ----------------------------------------- 数据字典管理 ----------------------------------------- //


    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    public void saveDataDictionary(DataDictionaryEntity entity) {
        if (Objects.nonNull(entity.getId())) {
            updateDataDictionary(entity, true);
        } else {
            insertDataDictionary(entity);
        }
    }

    /**
     * 更新数据字典
     *
     * @param entity        数据字典实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    public void updateDataDictionary(DataDictionaryEntity entity, boolean updateKeyPath) {

        setDataDictionaryParentCode(entity, updateKeyPath);

        DataDictionaryEntity dataDictionary = dataDictionaryService.get(entity.getId());

        if (!entity.getCode().equals(dataDictionary.getCode())) {

            DataDictionaryEntity exist = dataDictionaryService.getByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被数据字典[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DataDictionaryEntity> dataDictionaries = dataDictionaryService.findByParentId(dataDictionary.getId());

            for (DataDictionaryEntity dd : dataDictionaries) {
                String newKey = StringUtils.replace(dd.getCode(), dataDictionary.getCode(), entity.getCode());
                dd.setCode(newKey);
                updateDataDictionary(dd, false);
            }
        }

        dataDictionaryService.updateById(entity);
    }

    /**
     * 新增数据字典
     *
     * @param entity 数据字典实体
     */
    public void insertDataDictionary(DataDictionaryEntity entity) {

        DictionaryTypeEntity type = dictionaryTypeService.get(entity.getTypeId());

        if (Objects.isNull(type)) {
            throw new ServiceException("找不到 ID 为 [" + entity.getTypeId() + "] 的字典类型");
        }

        setDataDictionaryParentCode(entity, true);

        DataDictionaryEntity dataDictionary = dataDictionaryService.getByCode(entity.getCode());

        if (Objects.nonNull(dataDictionary)) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dataDictionaryService.insert(entity);
    }

    /**
     * 设置数据字典父类键路径
     *
     * @param entity        数据字典实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDataDictionaryParentCode(DataDictionaryEntity entity, boolean updateKeyPath) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DataDictionaryEntity parent = Objects.requireNonNull(
                    dataDictionaryService.get(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + config.getDictionary().getSeparator())) {
                entity.setCode(parent.getCode() + config.getDictionary().getSeparator() + entity.getCode());
            }

        } else {
            DictionaryTypeEntity dictionaryType = dictionaryTypeService.get(entity.getTypeId());
            if (!entity.getCode().startsWith(dictionaryType.getCode() + config.getDictionary().getSeparator())) {
                entity.setCode(dictionaryType.getCode() + config.getDictionary().getSeparator() + entity.getCode());
            }
        }
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键 id 集合
     */
    public void deleteDataDictionary(List<Integer> ids) {

        Wrapper<DataDictionaryEntity> wrapper = Wrappers
                .<DataDictionaryEntity>lambdaQuery()
                .select(DataDictionaryEntity::getId)
                .in(DataDictionaryEntity::getParentId, ids);

        List<Integer> subIds = dataDictionaryService.findObjects(wrapper, Integer.class);

        if (CollectionUtils.isNotEmpty(subIds)) {
            deleteDataDictionary(subIds);
        }

        dataDictionaryService.deleteById(ids);

    }

    // ----------------------------------------- 字典类型管理 ----------------------------------------- //

    /**
     * 保存字典类型实体
     *
     * @param entity 字典类型实体
     */
    public void saveDictionaryType(DictionaryTypeEntity entity) {
        if (Objects.nonNull(entity.getId())) {
            updateDictionaryType(entity, true);
        } else {
            insertDictionaryType(entity);
        }
    }

    /**
     * 新增字典类型
     *
     * @param entity 字典类型实体
     */
    private void insertDictionaryType(DictionaryTypeEntity entity) {

        setDictionaryTypeParentCode(entity, true);

        if (Objects.nonNull(dictionaryTypeService.getByCode(entity.getCode()))) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dictionaryTypeService.insert(entity);
    }

    /**
     * 设置数据类型父类键路径
     *
     * @param entity        数据类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDictionaryTypeParentCode(DictionaryTypeEntity entity, boolean updateKeyPath) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DictionaryTypeEntity parent = Objects.requireNonNull(
                    dictionaryTypeService.get(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + config.getDictionary().getSeparator())) {
                entity.setCode(parent.getCode() + config.getDictionary().getSeparator() + entity.getCode());
            }

        }
    }

    /**
     * 更新字典类型
     *
     * @param entity        字典类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    public void updateDictionaryType(DictionaryTypeEntity entity, boolean updateKeyPath) {

        setDictionaryTypeParentCode(entity, updateKeyPath);

        DictionaryTypeEntity dictionaryType = dictionaryTypeService.get(entity.getId());

        if (!dictionaryType.getCode().equals(entity.getCode())) {

            DictionaryTypeEntity exist = dictionaryTypeService.getByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被字典类型[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DictionaryTypeEntity> dictionaryTypes = dictionaryTypeService.getByParentId(dictionaryType.getId());

            for (DictionaryTypeEntity dt : dictionaryTypes) {
                String newKey = StringUtils.replace(dt.getCode(), dictionaryType.getCode(), entity.getCode());
                dt.setCode(newKey);
                updateDictionaryType(dt, false);
            }

            List<DataDictionaryEntity> dataDictionaries = dataDictionaryService.findByTypeId(entity.getId());

            for (DataDictionaryEntity dataDictionary : dataDictionaries) {
                String newKey = StringUtils.replace(dataDictionary.getCode(), dictionaryType.getCode(), entity.getCode());
                dataDictionary.setCode(newKey);
                updateDataDictionary(dataDictionary, false);
            }
        }

        dictionaryTypeService.updateById(entity);
    }

    /**
     * 删除字典类型
     *
     * @param ids 主键 id 集合
     */
    public void deleteDictionaryType(List<Integer> ids) {
        Wrapper<DictionaryTypeEntity> wrapper = Wrappers
                .<DictionaryTypeEntity>lambdaQuery()
                .select(DictionaryTypeEntity::getId)
                .in(DictionaryTypeEntity::getParentId, ids);

        List<Integer> subIds = dictionaryTypeService.findObjects(wrapper, Integer.class);

        if (CollectionUtils.isNotEmpty(subIds)) {
            List<Integer> dataDictionaryIds = dataDictionaryService.findIdByTypeId(subIds);

            if (CollectionUtils.isNotEmpty(dataDictionaryIds)) {
                dataDictionaryService.deleteById(dataDictionaryIds);
            }
        }

        if (CollectionUtils.isNotEmpty(subIds)) {
            deleteDictionaryType(subIds);
        }

        dictionaryTypeService.deleteById(ids);
    }

}
