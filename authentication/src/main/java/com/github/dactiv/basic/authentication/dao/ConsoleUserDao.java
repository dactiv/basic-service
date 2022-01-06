
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.domain.entity.ConsoleUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_console_user 系统用户数据访问
 *
 * <p>Table: tb_console_user - 系统用户</p>
 *
 * @author maurice
 * @see ConsoleUserEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface ConsoleUserDao extends BaseMapper<ConsoleUserEntity> {

}
