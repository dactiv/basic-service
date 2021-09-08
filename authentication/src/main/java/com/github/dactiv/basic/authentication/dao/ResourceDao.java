
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.Resource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_resource 用户组资源数据访问
 *
 * <p>Table: tb_resource - 用户组资源</p>
 *
 * @see Resource
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface ResourceDao extends BaseMapper<Resource> {

    /**
     * 获取组关联资源实体集合
     *
     * @param groupId 组 id
     *
     * @return 资源实体集合
     */
    @Select(
            "<script>" +
            "SELECT " +
            "   r.id, " +
            "   r.name, " +
            "   r.code, " +
            "   r.application_name, " +
            "   r.type, " +
            "   r.version, " +
            "   r.value, " +
            "   r.authority, " +
            "   r.icon, " +
            "   r.parent_id, " +
            "   r.status, " +
            "   r.sort, " +
            "   r.creation_time, " +
            "   r.source, " +
            "   r.remark " +
            "FROM " +
            "   tb_resource r " +
            "LEFT JOIN " +
            "   tb_group_resource gr ON gr.resource_id = r.id " +
            "LEFT JOIN " +
            "   tb_group g ON gr.group_id = g.id " +
            "WHERE " +
            "   g.id = #{groupId}" +
            "<if test=\"applicationName != null and !''.equals(applicationName)\"> " +
            "   and r.application_name = #{applicationName}" +
            "</if>" +
            "</script>"
    )
    List<Resource> getGroupResources(@Param("groupId") Integer groupId,
                                     @Param("applicationName") String applicationName);

    /**
     * 获取系统用户关联(包含组关联，所有资源)资源实体集合
     *
     * @param userId         系统用户 id
     * @param sourceContains 来源类型集合
     * @param type           資源类型
     *
     * @return 资源实体集合
     */
    @Select(
            "<script>" +
            "SELECT DISTINCT " +
            "   r.id, " +
            "   r.name, " +
            "   r.code, " +
            "   r.application_name, " +
            "   r.type, " +
            "   r.version, " +
            "   r.value, " +
            "   r.authority, " +
            "   r.icon, " +
            "   r.parent_id, " +
            "   r.status, " +
            "   r.sort, " +
            "   r.creation_time, " +
            "   r.source, " +
            "   r.remark " +
            "FROM " +
            "   tb_resource r " +
            "LEFT JOIN " +
            "   tb_group_resource gr ON gr.resource_id = r.id " +
            "LEFT JOIN " +
            "   tb_group g ON gr.group_id = g.id " +
            "LEFT JOIN " +
            "   tb_group_console_user gu ON gu.group_id = g.id " +
            "LEFT JOIN " +
            "   tb_console_user u ON gu.user_id = u.id " +
            "WHERE " +
            "   (u.id = #{userId} OR r.id in ( " +
            "      SELECT " +
            "      ur.resource_id " +
            "      FROM " +
            "      tb_console_user_resource ur " +
            "      WHERE " +
            "      ur.user_id = #{userId} " +
            "      ) " +
            "   ) " +
            "   AND " +
            "   r.status = 1" +
            "<if test='sourceContains != null and sourceContains.size &gt; 0'>" +
            "   AND r.source IN" +
            "   <foreach collection='sourceContains' item='s' open='(' close=')' separator=','>" +
            "       #{s}" +
            "   </foreach>" +
            "</if>" +
            "<if test='type != null'>" +
            "    AND r.type = #{type} " +
            "</if>" +
            "ORDER BY " +
            "   r.sort" +
            "</script>"
    )
    List<Resource> getConsolePrincipalResources(@Param("userId") Integer userId,
                                                @Param("sourceContains") List<String> sourceContains,
                                                @Param("type") String type);

    /**
     * 获取系统用户关联资源实体集合
     *
     * @param userId 系统用户 id
     *
     * @return 资源实体集合
     */
    @Select(
            "<script>" +
            "SELECT DISTINCT " +
            "   r.id, " +
            "   r.name, " +
            "   r.code, " +
            "   r.application_name, " +
            "   r.type, " +
            "   r.version, " +
            "   r.value, " +
            "   r.authority, " +
            "   r.icon, " +
            "   r.parent_id, " +
            "   r.status, " +
            "   r.sort, " +
            "   r.creation_time, " +
            "   r.source, " +
            "   r.remark " +
            "FROM " +
            "   tb_resource r " +
            "LEFT JOIN " +
            "   tb_console_user_resource ur ON ur.resource_id = r.id " +
            "LEFT JOIN " +
            "   tb_console_user u ON ur.user_id = u.id " +
            "WHERE " +
            "   u.id = #{userId} " +
            "GROUP BY " +
            "   r.id " +
            "ORDER BY " +
            "   r.sort " +
            "</script>"
    )
    List<Resource> getConsoleUserResources(@Param("userId") Integer userId);

    /**
     * 删除资源与组的关联
     *
     * @param id 资源 id
     */
    @Delete("<script>DELETE FROM tb_group_resource WHERE resource_id = #{id}</script>")
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 删除资源与系统用户的关联
     *
     * @param id 资源 id
     */
    @Delete("<script>DELETE FROM tb_console_user_resource WHERE resource_id = #{id}</script>")
    void deleteConsoleUserAssociation(@Param("id") Integer id);

}
