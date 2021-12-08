package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.basic.authentication.dao.GroupDao;
import com.github.dactiv.basic.authentication.entity.Group;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_group 的业务逻辑
 *
 * <p>Table: tb_group - 用户组表</p>
 *
 * @see Group
 *
 * @author maurice.chen
 *
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService extends BasicService<GroupDao, Group> {

}
