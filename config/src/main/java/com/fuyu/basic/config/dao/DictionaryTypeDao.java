package com.fuyu.basic.config.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.config.dao.entity.DictionaryType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典类型数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface DictionaryTypeDao extends BasicCurdDao<DictionaryType, Integer> {
}

