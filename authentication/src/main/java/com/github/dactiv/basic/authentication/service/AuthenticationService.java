package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.dao.AuthenticationInfoDao;
import com.github.dactiv.basic.authentication.entity.AuthenticationInfo;
import com.github.dactiv.basic.commons.feign.message.MessageService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.index.support.DateIndexGenerator;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 认证信息表管理服务
 *
 * @author maurice
 * @since 2020-06-01 08:20:59
 */
@Slf4j
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationInfoDao authenticationInfoDao;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private final DateIndexGenerator dateIndexGenerator = new DateIndexGenerator(
            AuthenticationInfo.DEFAULT_INDEX,
            "-",
            NumberIdEntity.CREATION_TIME_FIELD_NAME
    );

    /**
     * 保存认证信息表实体
     *
     * @param authenticationInfo 认证信息表实体
     */
    public void saveAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        if (Objects.isNull(authenticationInfo.getId())) {
            insertAuthenticationInfo(authenticationInfo);
        } else {
            updateAuthenticationInfo(authenticationInfo);
        }
    }

    /**
     * 新增认证信息表实体
     *
     * @param authenticationInfo 认证信息表实体
     */
    public void insertAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        authenticationInfoDao.insert(authenticationInfo);
    }

    /**
     * 更新认证信息表实体
     *
     * @param authenticationInfo 认证信息表实体
     */
    public void updateAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        authenticationInfoDao.updateById(authenticationInfo);
    }

    /**
     * 删除认证信息表实体
     *
     * @param id 主键 id
     */
    public void deleteAuthenticationInfo(Integer id) {
        authenticationInfoDao.deleteById(id);
    }

    /**
     * 删除认证信息表实体
     *
     * @param ids 主键 id 集合
     */
    public void deleteAuthenticationInfo(List<Integer> ids) {
        for (Integer id : ids) {
            deleteAuthenticationInfo(id);
        }
    }

    /**
     * 获取认证信息表实体
     *
     * @param id 主键 id
     *
     * @return 认证信息表实体
     */
    public AuthenticationInfo getAuthenticationInfo(Integer id) {
        return authenticationInfoDao.selectById(id);
    }

    /**
     * 获取最后一条认证信息表实体
     *
     * @param userId 用户 id
     * @param types  类型
     *
     * @return 认证信息表实体
     */
    public AuthenticationInfo getLastAuthenticationInfo(Integer userId, List<String> types) {
        return authenticationInfoDao.selectOne(Wrappers
                .<AuthenticationInfo>lambdaQuery()
                .eq(AuthenticationInfo::getUserId, userId)
                .in(AuthenticationInfo::getType, types)
        );
    }

    /**
     * 查找认证信息表实体分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper     查询包装器
     *
     * @return 分页实体
     */
    public Page<AuthenticationInfo> findAuthenticationInfoPage(PageRequest pageRequest, Wrapper<AuthenticationInfo> wrapper) {

        PageDto<AuthenticationInfo> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);
        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<AuthenticationInfo> result = authenticationInfoDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 验证认证信息
     *
     * @param info 认证信息
     */
    public void validAuthenticationInfo(AuthenticationInfo info) {

        Page<AuthenticationInfo> page = findAuthenticationInfoPage(
                new PageRequest(0, 1),
                Wrappers.
                        <AuthenticationInfo>lambdaQuery()
                        .eq(AuthenticationInfo::getUserId, info.getUserId())
                        .in(AuthenticationInfo::getType, Collections.singletonList(info.getType()))
                        .ne(AuthenticationInfo::getId, info.getId())
        );

        Iterator<AuthenticationInfo> iterator = page.getContent().iterator();

        AuthenticationInfo authenticationInfo = iterator.hasNext() ? iterator.next() : null;

        if (Objects.isNull(authenticationInfo)) {
            return;
        }

        if (authenticationInfo.equals(info)) {
            return;
        }

        Map<String, Object> param = new LinkedHashMap<>();

        param.put(MessageService.DEFAULT_MESSAGE_TYPE_KEY, applicationConfig.getAbnormalArea().getSendType());

        param.put("content", applicationConfig.getAbnormalArea().getSendContent());
        param.put("toUserIds", Collections.singletonList(info.getUserId()));
        param.put("type", applicationConfig.getAbnormalArea().getMessageType());
        param.put("title", applicationConfig.getAbnormalArea().getTitle());
        param.put("data", info.getDevice());
        param.put("isPush", YesOrNo.Yes.getValue());

        try {

            RestResult<Object> result = messageService.send(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }

        } catch (Exception e) {
            log.warn("发送站内信错误", e);
        }

    }

    @NacosCronScheduled(cron = "${authentication.extend.sync.auth-info-cron:0 0/3 * * * ? }", name = "同步认证信息")
    @Concurrent(value = "sync:authentication:info", exception = "同步认证信息遇到并发，不执行重试操作", waitTime = @Time(0L))
    public void syncAuthenticationInfo() {

        Page<AuthenticationInfo> page = findAuthenticationInfoPage(
                new PageRequest(1, 100),
                Wrappers.
                        <AuthenticationInfo>lambdaQuery()
                        .le(AuthenticationInfo::getRetryCount, applicationConfig.getAbnormalArea().getMaxRetryCount())
                        .ne(AuthenticationInfo::getSyncStatus, ExecuteStatus.Success.getValue())
        );

        LOGGER.info("开始同步" + page.getNumberOfElements() + "认证信息到 es");

        page.getContent().forEach(this::onAuthenticationSuccess);

    }

    public void onAuthenticationSuccess(AuthenticationInfo info) {
        try {

            info.setSyncStatus(ExecuteStatus.Failure.getValue());

            String index = dateIndexGenerator.generateIndex(info);

            IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(index));

            if (!indexOperations.exists()) {
                indexOperations.create();
                indexOperations.createMapping(info.getClass());
            }

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(info.getId().toString())
                    .withObject(info)
                    .build();

            elasticsearchRestTemplate.index(indexQuery, IndexCoordinates.of(index));

            info.setSyncStatus(ExecuteStatus.Success.getValue());

        } catch (Exception e) {
            info.setSyncStatus(ExecuteStatus.Failure.getValue());
            info.setRemark(e.getMessage());
            LOGGER.error("解析 ID 为 [" + info.getUserId() + "]的用户认证信息数据出错", e);
        }

        saveAuthenticationInfo(info);
    }

}
