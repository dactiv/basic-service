package com.fuyu.basic.authentication.dao;

import com.fuyu.basic.authentication.dao.entity.MemberUser;
import com.fuyu.basic.commons.BasicCurdDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会员用户数据访问
 *
 * @author maurice
 */
@Mapper
@Repository
public interface MemberUserDao extends BasicCurdDao<MemberUser, Integer> {

    /**
     * 更新会员用户登陆密码
     *
     * @param id       用户主键 ID
     * @param password 密码
     */
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 更新会员用户登陆账户
     *
     * @param id       用户主键 ID
     * @param username 密码
     */
    void updateUsername(@Param("id") Integer id, @Param("password") String username);

    /**
     * 删除会员用户组关联
     *
     * @param id 用户主键 ID
     */
    void deleteGroupAssociation(@Param("id") Integer id);

    /**
     * 新增会员用户组关联
     *
     * @param id       用户主键 ID
     * @param groupIds 用户组主键ID集合
     */
    void insertGroupAssociation(@Param("id") Integer id, @Param("groupIds") List<Integer> groupIds);

    /**
     * 根据唯一识别获取会员用户
     *
     * @param identified 唯一识别
     * @return 会员用户
     */
    MemberUser getByIdentified(@Param("identified") String identified);
}
