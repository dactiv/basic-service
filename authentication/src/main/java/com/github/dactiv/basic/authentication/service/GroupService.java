package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.dao.GroupDao;
import com.github.dactiv.basic.authentication.domain.entity.GroupEntity;

import com.github.dactiv.basic.authentication.domain.model.ResourceModel;
import com.github.dactiv.basic.commons.enumeration.ResourceSource;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * tb_group 的业务逻辑
 *
 * <p>Table: tb_group - 用户组表</p>
 *
 * @see GroupEntity
 *
 * @author maurice.chen
 *
 * @since 2021-11-25 02:42:57
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService extends BasicService<GroupDao, GroupEntity> {

    private final AuthorizationService authorizationService;

    private final ApplicationConfig config;

    public GroupService(AuthorizationService authorizationService, ApplicationConfig config) {
        this.authorizationService = authorizationService;
        this.config = config;
    }

    @Override
    public int save(GroupEntity entity) {

        List<ResourceModel> groupResource = authorizationService.getGroupResource(entity);

        List<String> noneMatchSources = groupResource
                .stream()
                .filter(r -> r.getSources().stream().noneMatch(s -> entity.getSources().contains(s)))
                .distinct()
                .flatMap(r -> r.getSources().stream().filter(s -> !entity.getSources().contains(s)))
                .map(ResourceSource::getName)
                .collect(Collectors.toList());

        if (!noneMatchSources.isEmpty()) {

            List<String> sourceNames = entity
                    .getSources()
                    .stream()
                    .map(ResourceSource::getName)
                    .collect(Collectors.toList());

            throw new ServiceException("组来源 " + sourceNames + " 不能保存属于 " + noneMatchSources + " 的资源");
        }

        return super.save(entity);
    }

    @Override
    public int insert(GroupEntity entity) {
        GroupEntity exist = lambdaQuery()
                .eq(GroupEntity::getName, entity.getName())
                .or()
                .eq(GroupEntity::getAuthority, entity.getAuthority())
                .one();

        if (Objects.nonNull(exist)) {
            throw new ServiceException("用户组名称 [" + entity.getName() + "] 或 authority 值 ["+ entity.getAuthority()+"] 已存在");
        }

        return super.insert(entity);
    }

    @Override
    public int updateById(GroupEntity entity) {
        GroupEntity exist = get(entity.getId());

        if (YesOrNo.No.equals(exist.getModifiable())) {
            throw new ServiceException("用户组 [" + exist.getName() + "] 不可修改");
        }

        int result = super.updateById(entity);
        if (result > 0) {
            authorizationService.deleteAuthorizationCache(exist.getSources());
        }
        return result;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {

        List<GroupEntity> groups = get(ids);

        List<GroupEntity> unRemovables = groups
                .stream()
                .filter(g -> YesOrNo.No.equals(g.getRemovable()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(unRemovables)) {
            throw new ServiceException("用户组 " + unRemovables.stream().map(GroupEntity::getName) + " 不可删除");
        }

        if (groups.stream().anyMatch(g -> g.getId().equals(config.getAdminGroupId()))) {
            throw new ServiceException("不能删除管理员组");
        }

        return super.deleteById(ids, errorThrow);
    }
}
