package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface DataDictionaryDao extends BaseMapper<DataDictionary> {

}
