package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.basic.authentication.dao.DepartmentDao;
import com.github.dactiv.basic.authentication.domain.entity.DepartmentEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_department 的业务逻辑
 *
 * <p>Table: tb_department - 部门表</p>
 *
 * @see DepartmentEntity
 *
 * @author maurice.chen
 *
 * @since 2022-02-09 06:47:53
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DepartmentService extends BasicService<DepartmentDao, DepartmentEntity> {

}
