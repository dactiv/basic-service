
package com.github.dactiv.basic.authentication.dao;

import com.github.dactiv.basic.authentication.dao.entity.Resource;
import com.github.dactiv.framework.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 用户组资源数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface ResourceDao extends BasicCurdDao<Resource, Integer> {

    /**
     * 获取组关联资源实体集合
     *
     * @param groupId 组 id
     * @return 资源实体集合
     */
    List<Resource> getGroupResources(@Param("groupId") Integer groupId);

    /**
     * 获取系统用户关联(包含组关联，所有资源)资源实体集合
     *
     * @param userId 系统用户 id
     * @param filter 过滤条件
     * @return 资源实体集合
     */
    List<Resource> getConsolePrincipalResources(@Param("userId") Integer userId, @Param("filter") Map<String, Object> filter);

    /**
     * 获取系统用户关联资源实体集合
     *
     * @param userId 系统用户 id
     * @return 资源实体集合
     */
    List<Resource> getConsoleUserResources(@Param("userId") Integer userId);

    /**
     * 删除资源与组的关联
     *
     * @param id 资源 id
     */
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 删除资源与系统用户的关联
     *
     * @param id 资源 id
     */
    void deleteConsoleUserAssociation(@Param("id") Integer id);

}
