package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.MemberUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_member_user 会员用户数据访问
 *
 * <p>Table: tb_member_user - 会员用户</p>
 *
 * @see MemberUser
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface MemberUserDao extends BaseMapper<MemberUser> {

}
