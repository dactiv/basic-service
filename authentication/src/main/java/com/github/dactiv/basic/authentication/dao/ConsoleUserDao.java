
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.entity.ConsoleUser;
import org.apache.ibatis.annotations.*;
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

    /**
     * 删除系统用户组关联
     *
     * @param id 用户主键 ID
     */
    @Delete("<script>DELETE FROM tb_group_console_user WHERE user_id = #{id}</script>")
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 新增系统用户组关联
     *
     * @param id       用户主键 ID
     * @param groupIds 用户组主键ID集合
     */
    @Insert(
            "<script>" +
            "INSERT INTO " +
            "   tb_group_console_user(user_id,group_id) " +
            "VALUES" +
            "<foreach collection='groupIds' item='groupId' separator=','>" +
            "   (#{id}, #{groupId}) " +
            "</foreach>" +
            "</script>"
    )
    void insertGroupAssociation(@Param("id") Integer id, @Param("groupIds") List<Integer> groupIds);

    /**
     * 删除系统用户组资源关联
     *
     * @param id 用户主键 ID
     */
    @Delete("<script>DELETE FROM tb_console_user_resource WHERE user_id = #{id}</script>")
    void deleteResourceAssociation(@Param("id") Integer id);

    /**
     * 新增系统用户组资源关联
     *
     * @param id          用户主键 ID
     * @param resourceIds 用户组资源主键ID集合
     */
    @Insert(
            "<script>" +
            "INSERT INTO " +
            "   tb_console_user_resource(user_id,resource_id) " +
            "VALUES " +
            "<foreach collection='resourceIds' item='resourceId' separator=','>" +
            "   (#{id}, #{resourceId}) " +
            "</foreach>" +
            "</script>"
    )
    void insertResourceAssociation(@Param("id") Integer id, @Param("resourceIds") List<Integer> resourceIds);

    /**
     * 通过用户组查询系统用户集合
     *
     * @param groupId 组 id
     *
     * @return 系统用户集合
     */
    @Select(
            "SELECT " +
            "   cu.id, " +
            "   cu.creation_time, " +
            "   cu.email, " +
            "   cu.password, " +
            "   cu.status, " +
            "   cu.username, " +
            "   cu.real_name, " +
            "   cu.remark " +
            "FROM " +
            "   tb_console_user cu " +
            "INNER JOIN " +
            "   tb_group_console_user gcu " +
            "ON " +
            "   gcu.user_id = cu.id " +
            "WHERE " +
            "   gcu.group_id = #{groupId} "
    )
    List<ConsoleUser> findByGroupId(@Param("groupId")String groupId);
}
