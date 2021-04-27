
package com.github.dactiv.basic.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.authentication.dao.entity.Group;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户组数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface GroupDao extends BaseMapper<Group> {

    /**
     * 获取系统用户所关联用户组
     *
     * @param userId 用户主键 ID
     *
     * @return 用户组实体集合
     */
    @Select(
            "<script>" +
                    "SELECT " +
                    "   g.id, " +
                    "   g.name, " +
                    "   g.authority, " +
                    "   g.source, " +
                    "   g.parent_id, " +
                    "   g.removable, " +
                    "   g.modifiable, " +
                    "   g.remark " +
                    "FROM " +
                    "   tb_group g " +
                    "LEFT JOIN " +
                    "   tb_group_console_user gu ON g.id = gu.group_id " +
                    "LEFT JOIN " +
                    "   tb_console_user u ON u.id = gu.user_id " +
                    "WHERE " +
                    "   u.id = #{userId} " +
                    "</script>"
    )
    List<Group> getConsoleUserGroups(@Param("userId") Integer userId);

    /**
     * 获取会员用户所关联用户组
     *
     * @param userId 用户主键 ID
     *
     * @return 用户组实体集合
     */
    @Select(
            "<script>" +
            "SELECT " +
            "   g.id, " +
            "   g.name, " +
            "   g.authority, " +
            "   g.source, " +
            "   g.parent_id, " +
            "   g.removable, " +
            "   g.modifiable, " +
            "   g.remark " +
            "FROM " +
            "   tb_group g " +
            "LEFT JOIN " +
            "  tb_group_member_user gu ON g.id = gu.group_id " +
            "LEFT JOIN " +
            "  tb_member_user u ON u.id = gu.user_id " +
            "WHERE " +
            "u.id = #{userId} " +
            "</script>"
    )
    List<Group> getMemberUserGroups(@Param("userId") Integer userId);

    /**
     * 新增组与用户组资源的关联
     *
     * @param id          用户组主键 ID
     * @param resourceIds 关联的用户组资源主键 ID 集合
     */
    @Insert(
            "<script>" +
            "INSERT INTO " +
            "tb_group_resource(group_id,resource_id) " +
            "VALUES " +
            "<foreach collection='resourceIds' item='resourceId' separator=','>" +
            "    (#{id}, #{resourceId}) " +
            "</foreach>" +
            "</script>"
    )
    void insertResourceAssociation(@Param("id") Integer id, @Param("resourceIds") List<Integer> resourceIds);

    /**
     * 删除用户组与户组资源的关联
     *
     * @param id 主键 ID
     */
    @Delete("<script>DELETE FROM tb_group_resource WHERE group_id = #{id}</script>")
    void deleteResourceAssociation(@Param("id") Integer id);

    /**
     * 删除用户组与系统用户的关联
     *
     * @param id 用户组主键 ID
     */
    @Delete("<script>DELETE FROM tb_group_console_user WHERE group_id = #{id}</script>")
    void deleteConsoleUserAssociation(@Param("id") Integer id);

    /**
     * 删除用户组与会员用户的关联
     *
     * @param id 用户组主键 ID
     */
    @Delete("<script>DELETE FROM tb_group_member_user WHERE group_id = #{id}</script>")
    void deleteMemberUserAssociation(@Param("id") Integer id);
}
