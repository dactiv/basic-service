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

    /**
     * 删除会员用户组关联
     *
     * @param id 用户主键 ID
     */
    @Delete("<script>DELETE FROM tb_group_member_user WHERE user_id = #{id}</script>")
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 新增会员用户组关联
     *
     * @param id       用户主键 ID
     * @param groupIds 用户组主键ID集合
     */
    @Insert(
            "<script>" +
            "INSERT INTO " +
            "   tb_group_member_user(user_id,group_id) " +
            "VALUES " +
            "<foreach collection='groupIds' item='groupId' separator=','>" +
            "    (#{id}, #{groupId}) " +
            "</foreach>" +
            "</script>"
    )
    void insertGroupAssociation(@Param("id") Integer id, @Param("groupIds") List<Integer> groupIds);
}
