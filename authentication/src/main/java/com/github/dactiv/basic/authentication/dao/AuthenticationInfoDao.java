package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
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
public interface AuthenticationInfoDao extends BaseMapper<AuthenticationInfo> {

}
