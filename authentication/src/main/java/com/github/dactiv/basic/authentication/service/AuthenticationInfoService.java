package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.dao.AuthenticationInfoDao;
import com.github.dactiv.basic.authentication.entity.AuthenticationInfo;

import com.github.dactiv.basic.commons.feign.message.MessageService;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.index.support.DateIndexGenerator;
import lombok.extern.slf4j.Slf4j;
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
 *
 * tb_authentication_info 的业务逻辑
 *
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @see AuthenticationInfo
 *
 * @author maurice.chen
 *
 * @since 2021-11-25 02:42:57
 */
@Slf4j
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AuthenticationInfoService extends BasicService<AuthenticationInfoDao, AuthenticationInfo> {

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
     * 获取最后一条认证信息表实体
     *
     * @param userId 用户 id
     * @param types  类型
     *
     * @return 认证信息表实体
     */
    public AuthenticationInfo getLastAuthenticationInfo(Integer userId, List<String> types) {
        Wrapper<AuthenticationInfo> wrapper = lambdaQuery()
                .eq(AuthenticationInfo::getUserId, userId)
                .in(AuthenticationInfo::getType, types);
        return findOne(wrapper);
    }

    /**
     * 验证认证信息
     *
     * @param info 认证信息
     */
    public void validAuthenticationInfo(AuthenticationInfo info) {

        Wrapper<AuthenticationInfo> wrapper = lambdaQuery()
                .eq(AuthenticationInfo::getUserId, info.getUserId())
                .in(AuthenticationInfo::getType, Collections.singletonList(info.getType()))
                .ne(AuthenticationInfo::getId, info.getId());

        Page<AuthenticationInfo> page = findPage(new PageRequest(0, 1), wrapper);

        Iterator<AuthenticationInfo> iterator = page.getElements().iterator();

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
        Wrapper<AuthenticationInfo> wrapper = lambdaQuery()
                .le(AuthenticationInfo::getRetryCount, applicationConfig.getAbnormalArea().getMaxRetryCount())
                .ne(AuthenticationInfo::getSyncStatus, ExecuteStatus.Success.getValue());

        Page<AuthenticationInfo> page = findPage(new PageRequest(1, 100), wrapper);

        log.info("开始同步" + page.getNumberOfElements() + "认证信息到 es");

        page.getElements().forEach(this::onAuthenticationSuccess);

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
            log.error("解析 ID 为 [" + info.getUserId() + "]的用户认证信息数据出错", e);
        }

        save(info);
    }
}
