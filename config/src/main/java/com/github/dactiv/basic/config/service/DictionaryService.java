package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.github.dactiv.basic.config.dao.DataDictionaryDao;
import com.github.dactiv.basic.config.dao.DictionaryTypeDao;
import com.github.dactiv.basic.config.entity.DataDictionary;
import com.github.dactiv.basic.config.entity.DictionaryType;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class DictionaryService {

    @Autowired
    private DataDictionaryDao dataDictionaryDao;

    @Autowired
    private DictionaryTypeDao dictionaryTypeDao;

    @Value("${spring.application.dictionary.separator:.}")
    public String dictionarySeparator;

    // ----------------------------------------- 数据字典管理 ----------------------------------------- //

    /**
     * 获取数据字典
     *
     * @param id 数据字典 ID
     *
     * @return 字典实体
     */
    public DataDictionary getDataDictionary(Integer id) {
        return dataDictionaryDao.selectById(id);
    }

    /**
     * 获取数据字典
     *
     * @param code 字典代码
     *
     * @return 字典实体
     */
    public DataDictionary getDataDictionaryByCode(String code) {
        return dataDictionaryDao.selectOne(Wrappers.<DataDictionary>lambdaQuery().eq(DataDictionary::getCode, code));
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     *
     * @return 字典实体集合
     */
    public List<DataDictionary> getDataDictionariesByParentId(Integer parentId) {
        return findDataDictionaries(
                Wrappers
                        .<DataDictionary>lambdaQuery()
                        .eq(DataDictionary::getParentId, parentId)
        );
    }

    /**
     * 获取数据字典集合
     *
     * @param typeId 字典类型 id
     *
     * @return 字典实体集合
     */
    public List<DataDictionary> getDataDictionariesByTypeId(Integer typeId) {
        return findDataDictionaries(
                Wrappers
                        .<DataDictionary>lambdaQuery()
                        .eq(DataDictionary::getTypeId, typeId)
        );
    }

    /**
     * 查找数据字典
     *
     * @param wrapper 包装器
     *
     * @return 数据字典集合
     */
    public List<DataDictionary> findDataDictionaries(Wrapper<DataDictionary> wrapper) {
        return dataDictionaryDao.selectList(wrapper);
    }

    /**
     * 查找数据字典分页信息
     *
     * @param pageRequest 分页请求
     * @param wrapper     包装器
     *
     * @return 分页实体
     */
    public Page<DataDictionary> findDataDictionariesPage(PageRequest pageRequest, Wrapper<DataDictionary> wrapper) {

        PageDto<DataDictionary> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<DataDictionary> result = dataDictionaryDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    public void saveDataDictionary(DataDictionary entity) {
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
    public void updateDataDictionary(DataDictionary entity, boolean updateKeyPath) {

        setDataDictionaryParentCode(entity, updateKeyPath);

        DataDictionary dataDictionary = getDataDictionary(entity.getId());

        if (!entity.getCode().equals(dataDictionary.getCode())) {

            DataDictionary exist = getDataDictionaryByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被数据字典[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DataDictionary> dataDictionaries = getDataDictionariesByParentId(dataDictionary.getId());

            for (DataDictionary dd : dataDictionaries) {
                String newKey = StringUtils.replace(dd.getCode(), dataDictionary.getCode(), entity.getCode());
                dd.setCode(newKey);
                updateDataDictionary(dd, false);
            }
        }

        dataDictionaryDao.updateById(entity);
    }

    /**
     * 新增数据字典
     *
     * @param entity 数据字典实体
     */
    public void insertDataDictionary(DataDictionary entity) {

        DictionaryType type = getDictionaryType(entity.getTypeId());

        if (Objects.isNull(type)) {
            throw new ServiceException("找不到 ID 为 [" + entity.getTypeId() + "] 的字典类型");
        }

        setDataDictionaryParentCode(entity, true);

        DataDictionary dataDictionary = getDataDictionaryByCode(entity.getCode());

        if (Objects.nonNull(dataDictionary)) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dataDictionaryDao.insert(entity);
    }

    /**
     * 设置数据字典父类键路径
     *
     * @param entity        数据字典实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDataDictionaryParentCode(DataDictionary entity, boolean updateKeyPath) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DataDictionary parent = Objects.requireNonNull(
                    getDataDictionary(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + dictionarySeparator)) {
                entity.setCode(parent.getCode() + dictionarySeparator + entity.getCode());
            }

        } else {
            DictionaryType dictionaryType = getDictionaryType(entity.getTypeId());
            if (!entity.getCode().startsWith(dictionaryType.getCode() + dictionarySeparator)) {
                entity.setCode(dictionaryType.getCode() + dictionarySeparator + entity.getCode());
            }
        }
    }

    /**
     * 删除字典值
     *
     * @param ids 字典实体
     */
    public void deleteDataDictionaries(List<Integer> ids) {
        for (Integer id : ids) {
            deleteDataDictionary(id);
        }
    }

    /**
     * 根据父字典值删除字典值
     *
     * @param id 字典实体
     */
    private void deleteDataDictionary(Integer id) {

        List<DataDictionary> dataDictionaries = getDataDictionariesByParentId(id);

        for (DataDictionary dataDictionary : dataDictionaries) {
            deleteDataDictionary(dataDictionary.getId());
        }

        dataDictionaryDao.deleteById(id);
    }

    // ----------------------------------------- 字典类型管理 ----------------------------------------- //

    /**
     * 获取字典类型实体
     *
     * @param id 主键 ID
     *
     * @return 字典类型实体
     */
    public DictionaryType getDictionaryType(Integer id) {
        return dictionaryTypeDao.selectById(id);
    }

    /**
     * 获取字典类型实体
     *
     * @param code 代码
     *
     * @return 字典类型实体
     */
    public DictionaryType getDictionaryTypeByCode(String code) {
        return dictionaryTypeDao.selectOne(Wrappers.<DictionaryType>lambdaQuery().eq(DictionaryType::getCode, code));
    }

    /**
     * 获取字典类型实体
     *
     * @param parentId 父类 id
     *
     * @return 字典类型实体
     */
    public List<DictionaryType> getDictionaryTypesByParentId(Integer parentId) {
        return dictionaryTypeDao.selectList(Wrappers.<DictionaryType>lambdaQuery().eq(DictionaryType::getParentId, parentId));
    }

    /**
     * 查找字典类型
     *
     * @param wrapper 包装器
     *
     * @return 字典类型集合
     */
    public List<DictionaryType> findDictionaryTypes(Wrapper<DictionaryType> wrapper) {
        return dictionaryTypeDao.selectList(wrapper);
    }

    /**
     * 保存字典类型实体
     *
     * @param entity 字典类型实体
     */
    public void saveDictionaryType(DictionaryType entity) {
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
    private void insertDictionaryType(DictionaryType entity) {

        setDictionaryTypeParentCode(entity, true);

        if (Objects.nonNull(getDictionaryTypeByCode(entity.getCode()))) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dictionaryTypeDao.insert(entity);
    }

    /**
     * 设置数据类型父类键路径
     *
     * @param entity        数据类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDictionaryTypeParentCode(DictionaryType entity, boolean updateKeyPath) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DictionaryType parent = Objects.requireNonNull(
                    getDictionaryType(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + dictionarySeparator)) {
                entity.setCode(parent.getCode() + dictionarySeparator + entity.getCode());
            }

        }
    }

    /**
     * 更新字典类型
     *
     * @param entity        字典类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    public void updateDictionaryType(DictionaryType entity, boolean updateKeyPath) {

        setDictionaryTypeParentCode(entity, updateKeyPath);

        DictionaryType dictionaryType = getDictionaryType(entity.getId());

        if (!dictionaryType.getCode().equals(entity.getCode())) {

            DictionaryType exist = getDictionaryTypeByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被字典类型[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DictionaryType> dictionaryTypes = getDictionaryTypesByParentId(dictionaryType.getId());

            for (DictionaryType dt : dictionaryTypes) {
                String newKey = StringUtils.replace(dt.getCode(), dictionaryType.getCode(), entity.getCode());
                dt.setCode(newKey);
                updateDictionaryType(dt, false);
            }

            List<DataDictionary> dataDictionaries = getDataDictionariesByTypeId(entity.getId());

            for (DataDictionary dataDictionary : dataDictionaries) {
                String newKey = StringUtils.replace(dataDictionary.getCode(), dictionaryType.getCode(), entity.getCode());
                dataDictionary.setCode(newKey);
                updateDataDictionary(dataDictionary, false);
            }
        }

        dictionaryTypeDao.updateById(entity);
    }

    /**
     * 删除字典类型
     *
     * @param ids 要删除的字典ID集合
     */
    public void deleteDictionaryTypes(List<Integer> ids) {
        for (Integer id : ids) {
            deleteDictionaryType(id);
        }
    }

    /**
     * 根据字典类型
     *
     * @param id 字典ID
     */
    private void deleteDictionaryType(Integer id) {

        List<DictionaryType> dictionaryTypes = getDictionaryTypesByParentId(id);

        for (DictionaryType dictionaryType : dictionaryTypes) {
            deleteDictionaryType(dictionaryType.getId());
        }

        List<DataDictionary> dataDictionaries = getDataDictionariesByTypeId(id);

        for (DataDictionary dataDictionary : dataDictionaries) {
            deleteDataDictionary(dataDictionary.getId());
        }

        dictionaryTypeDao.deleteById(id);
    }


}
