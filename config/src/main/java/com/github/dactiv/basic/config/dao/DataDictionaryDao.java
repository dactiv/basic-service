package com.github.dactiv.basic.config.dao;

import com.github.dactiv.framework.commons.BasicCurdDao;
import com.github.dactiv.basic.config.dao.entity.DataDictionary;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典实体数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface DataDictionaryDao extends BasicCurdDao<DataDictionary, Integer> {

}
