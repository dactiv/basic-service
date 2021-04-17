
package com.github.dactiv.basic.authentication.dao;

import com.github.dactiv.basic.authentication.dao.entity.ConsoleUser;
import com.github.dactiv.framework.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统用户数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface ConsoleUserDao extends BasicCurdDao<ConsoleUser, Integer> {

    /**
     * 更新系统用户登陆密码
     *
     * @param id       用户主键 ID
     * @param password 密码
     */
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 删除系统用户组关联
     *
     * @param id 用户主键 ID
     */
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 新增系统用户组关联
     *
     * @param id       用户主键 ID
     * @param groupIds 用户组主键ID集合
     */
    void insertGroupAssociation(@Param("id") Integer id, @Param("groupIds") List<Integer> groupIds);

    /**
     * 删除系统用户组资源关联
     *
     * @param id 用户主键 ID
     */
    void deleteResourceAssociation(@Param("id") Integer id);

    /**
     * 新增系统用户组资源关联
     *
     * @param id          用户主键 ID
     * @param resourceIds 用户组资源主键ID集合
     */
    void insertResourceAssociation(@Param("id") Integer id, @Param("resourceIds") List<Integer> resourceIds);

}
