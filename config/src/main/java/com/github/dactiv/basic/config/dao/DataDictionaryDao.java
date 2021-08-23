package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.config.entity.DataDictionary;
import com.github.dactiv.basic.config.entity.DictionaryType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_data_dictionary 数据字典数据访问
 *
 * <p>Table: tb_data_dictionary - 数据字典</p>
 *
 * @see DataDictionary
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface DataDictionaryDao extends BaseMapper<DataDictionary> {

}
