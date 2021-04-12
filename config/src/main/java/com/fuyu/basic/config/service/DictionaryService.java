package com.fuyu.basic.config.service;

import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.page.Page;
import com.fuyu.basic.commons.page.PageRequest;
import com.fuyu.basic.config.dao.DataDictionaryDao;
import com.fuyu.basic.config.dao.DictionaryTypeDao;
import com.fuyu.basic.config.dao.entity.DataDictionary;
import com.fuyu.basic.config.dao.entity.DictionaryType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
     * @return 字典实体
     */
    public DataDictionary getDataDictionary(Integer id) {
        return dataDictionaryDao.get(id);
    }

    /**
     * 查找数据字典
     *
     * @param filter 过滤条件
     * @return 数据字典集合
     */
    public List<DataDictionary> findDataDictionaries(Map<String, Object> filter) {
        return dataDictionaryDao.find(filter);
    }

    /**
     * 获取数据字典
     *
     * @param filter 过滤条件
     * @return 数据字典
     */
    public DataDictionary getDataDictionaryByFilter(Map<String, Object> filter) {
        List<DataDictionary> result = findDataDictionaries(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<DataDictionary> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 查找数据字典分页信息
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<DataDictionary> findDataDictionariesPage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<DataDictionary> data = findDataDictionaries(filter);

        return new Page<>(pageRequest, data);
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

            DataDictionary exist = getDataDictionaryByFilter(entity.getUniqueFilter());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被数据字典[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            Map<String, Object> filter = new HashMap<>(16);
            filter.put("parentIdEq", dataDictionary.getId());
            List<DataDictionary> dataDictionaries = findDataDictionaries(filter);

            for (DataDictionary dd : dataDictionaries) {
                String newKey = StringUtils.replace(dd.getCode(), dataDictionary.getCode(), entity.getCode());
                dd.setCode(newKey);
                updateDataDictionary(dd, false);
            }
        }

        dataDictionaryDao.update(entity);
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

        DataDictionary dataDictionary = getDataDictionaryByFilter(entity.getUniqueFilter());

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

        Map<String, Object> filter = new HashMap<>(16);
        filter.put("parentIdEq", id);

        List<DataDictionary> dataDictionaries = findDataDictionaries(filter);

        for (DataDictionary dataDictionary : dataDictionaries) {
            deleteDataDictionary(dataDictionary.getId());
        }

        dataDictionaryDao.delete(id);
    }

    // ----------------------------------------- 字典类型管理 ----------------------------------------- //

    /**
     * 获取字典类型实体
     *
     * @param id 主键 ID
     * @return 字典类型实体
     */
    public DictionaryType getDictionaryType(Integer id) {
        return dictionaryTypeDao.get(id);
    }


    /**
     * 查找字典类型
     *
     * @param filter 过滤条件
     * @return 字典类型集合
     */
    public List<DictionaryType> findDictionaryTypes(Map<String, Object> filter) {
        return dictionaryTypeDao.find(filter);
    }

    /**
     * 获取字典类型
     *
     * @param filter 过滤条件
     * @return 字典类型
     */
    public DictionaryType getDictionaryTypeByFilter(Map<String, Object> filter) {
        List<DictionaryType> result = findDictionaryTypes(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<DictionaryType> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
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

        if (Objects.nonNull(getDictionaryTypeByFilter(entity.getUniqueFilter()))) {
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

            DictionaryType exist = getDictionaryTypeByFilter(entity.getUniqueFilter());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被字典类型[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            Map<String, Object> filter = new HashMap<>(16);

            filter.put("parentIdEq", dictionaryType.getId());

            List<DictionaryType> dictionaryTypes = findDictionaryTypes(filter);

            for (DictionaryType dt : dictionaryTypes) {
                String newKey = StringUtils.replace(dt.getCode(), dictionaryType.getCode(), entity.getCode());
                dt.setCode(newKey);
                updateDictionaryType(dt, false);
            }

            filter.clear();
            filter.put("typeIdEq", entity.getId());
            List<DataDictionary> dataDictionaries = findDataDictionaries(filter);

            for (DataDictionary dataDictionary : dataDictionaries) {
                String newKey = StringUtils.replace(dataDictionary.getCode(), dictionaryType.getCode(), entity.getCode());
                dataDictionary.setCode(newKey);
                updateDataDictionary(dataDictionary, false);
            }
        }

        dictionaryTypeDao.update(entity);
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

        Map<String, Object> filter = new HashMap<>(16);
        filter.put("parentIdEq", id);
        List<DictionaryType> dictionaryTypes = findDictionaryTypes(filter);

        for (DictionaryType dictionaryType : dictionaryTypes) {
            deleteDictionaryType(dictionaryType.getId());
        }

        filter.clear();

        filter.put("typeIdEq", id);

        List<DataDictionary> dataDictionaries = findDataDictionaries(filter);

        for (DataDictionary dataDictionary : dataDictionaries) {
            deleteDataDictionary(dataDictionary.getId());
        }

        dictionaryTypeDao.delete(id);
    }


}
