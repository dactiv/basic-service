package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.MemberUser;
import com.github.dactiv.basic.authentication.entity.MemberUserInitialization;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_member_user_initialization 用户初始化数据访问
 *
 * <p>Table: tb_member_user_initialization - 用户初始化</p>
 *
 * @see MemberUserInitialization
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface MemberUserInitializationDao extends BaseMapper<MemberUserInitialization> {

}
