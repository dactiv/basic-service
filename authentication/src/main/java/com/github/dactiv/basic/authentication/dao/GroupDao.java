
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_group 用户组数据访问
 *
 * <p>Table: tb_group - 用户组</p>
 *
 * @see Group
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface GroupDao extends BaseMapper<Group> {

}
