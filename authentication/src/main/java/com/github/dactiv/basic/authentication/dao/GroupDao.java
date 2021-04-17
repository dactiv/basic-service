
package com.github.dactiv.basic.authentication.dao;

import com.github.dactiv.basic.authentication.dao.entity.Group;
import com.github.dactiv.framework.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户组数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface GroupDao extends BasicCurdDao<Group, Integer> {

    /**
     * 获取系统用户所关联用户组
     *
     * @param userId 用户主键 ID
     * @return 用户组实体集合
     */
    List<Group> getConsoleUserGroups(@Param("userId") Integer userId);

    /**
     * 获取会员用户所关联用户组
     *
     * @param userId 用户主键 ID
     * @return 用户组实体集合
     */
    List<Group> getMemberUserGroups(@Param("userId") Integer userId);

    /**
     * 新增组与用户组资源的关联
     *
     * @param id          用户组主键 ID
     * @param resourceIds 关联的用户组资源主键 ID 集合
     */
    void insertResourceAssociation(@Param("id") Integer id, @Param("resourceIds") List<Integer> resourceIds);

    /**
     * 删除用户组与户组资源的关联
     *
     * @param id 主键 ID
     */
    void deleteResourceAssociation(@Param("id") Integer id);

    /**
     * 删除用户组与系统用户的关联
     *
     * @param id 用户组主键 ID
     */
    void deleteConsoleUserAssociation(@Param("id") Integer id);

    /**
     * 删除用户组与会员用户的关联
     *
     * @param id 用户组主键 ID
     */
    void deleteMemberUserAssociation(@Param("id") Integer id);
}
