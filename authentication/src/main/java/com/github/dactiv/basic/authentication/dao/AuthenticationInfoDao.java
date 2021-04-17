package com.github.dactiv.basic.authentication.dao;

import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.framework.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 认证信息数据访问
 *
 * @author maurice
 * @since 2020-06-01 08:20:59
 */
@Mapper
@Repository
public interface AuthenticationInfoDao extends BasicCurdDao<AuthenticationInfo, Integer> {

}
