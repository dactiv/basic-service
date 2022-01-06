package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.domain.entity.MemberUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_member_user 会员用户数据访问
 *
 * <p>Table: tb_member_user - 会员用户</p>
 *
 * @author maurice
 * @see MemberUserEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface MemberUserDao extends BaseMapper<MemberUserEntity> {

}
