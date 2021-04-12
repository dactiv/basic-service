package com.fuyu.basic.authentication.dao;

import com.fuyu.basic.authentication.dao.entity.MemberUserInitialization;
import com.fuyu.basic.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 用户初始化数据访问
 *
 * @author maurice
 * @since 2020-04-13 09:51:39
 */
@Mapper
@Repository
public interface MemberUserInitializationDao extends BasicCurdDao<MemberUserInitialization, Integer> {

}
