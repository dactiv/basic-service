
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_console_user 系统用户数据访问
 *
 * <p>Table: tb_console_user - 系统用户</p>
 *
 * @see ConsoleUser
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface ConsoleUserDao extends BaseMapper<ConsoleUser> {

}
