package com.github.dactiv.basic.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.authentication.dao.AuthenticationInfoDao;
import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.spring.security.concurrent.annotation.ConcurrentProcess;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
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

    private static final String DEFAULT_ES_INDEX = "authentication-info";

    @Autowired
    private AuthenticationInfoDao authenticationInfoDao;

    @Autowired
    private MessageService messageService;

    @Value("${spring.security.authentication.offsite.send-content:您的账户在异地登录，如果非本人操作。请及时修改密码。}")
    private String sendContent;

    @Value("${spring.security.authentication.offsite.title:异地登录通知}")
    private String title;

    @Value("${spring.security.authentication.offsite.message-type:system}")
    private String messageType;

    @Value("${spring.security.authentication.offsite.from-user-id:1}")
    private Integer fromUserId;

    @Value("${spring.security.authentication.offsite.max-retry-count:3}")
    private Integer maxRetryCount;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
     * @param pageable 分页请求
     * @param wrapper  查询包装器
     *
     * @return 分页实体
     */
    public Page<AuthenticationInfo> findAuthenticationInfoPage(Pageable pageable, Wrapper<AuthenticationInfo> wrapper) {

        IPage<AuthenticationInfo> result = authenticationInfoDao.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageable),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 验证认证信息
     *
     * @param info 认证信息
     */
    public void validAuthenticationInfo(AuthenticationInfo info) {

        Page<AuthenticationInfo> page = findAuthenticationInfoPage(
                PageRequest.of(0,1, Sort.by(Sort.Order.asc("id"))),
                Wrappers.
                        <AuthenticationInfo>lambdaQuery()
                        .eq(AuthenticationInfo::getUserId, info.getUserId())
                        .in(AuthenticationInfo::getType, Collections.singletonList(info.getType()))
                        .ne(AuthenticationInfo::getId, info.getId())
        );

        Iterator<AuthenticationInfo> iterator = page.iterator();

        AuthenticationInfo authenticationInfo = iterator.hasNext() ? iterator.next() : null;

        if (Objects.nonNull(authenticationInfo) && !authenticationInfo.equals(info) && info.getId() > authenticationInfo.getId()) {

            Map<String, Object> param = new LinkedHashMap<>();

            param.put(MessageService.DEFAULT_MESSAGE_TYPE_KEY, MessageService.DEFAULT_MESSAGE_TYPE_VALUE);

            param.put("content", sendContent);
            param.put("fromUserId", fromUserId);
            param.put("toUserId", info.getUserId());
            param.put("type", messageType);
            param.put("title", title);
            param.put("data", objectMapper.convertValue(info, Map.class));
            param.put("pushMessage", YesOrNo.Yes.getValue());

            try {

                RestResult<Map<String, Object>> result = messageService.sendMessage(param);

                if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                    throw new ServiceException(result.getMessage());
                }

            } catch (Exception e) {
                log.warn("发送站内信错误", e);
            }

        }
    }

    @ConcurrentProcess(value = "sync.authentication.info", exceptionMessage = "同步认证信息遇到并发，不执行重试操作")
    @Scheduled(cron = "${dynamic.retry.cron.expression:0 0/3 * * * ? }")
    public void syncAuthenticationInfo() {

        Page<AuthenticationInfo> page = findAuthenticationInfoPage(
                PageRequest.of(1,100),
                Wrappers.
                        <AuthenticationInfo>lambdaQuery()
                        .le(AuthenticationInfo::getRetryCount, maxRetryCount)
                        .ne(AuthenticationInfo::getSyncStatus, ExecuteStatus.Success.getValue())
        );

        LOGGER.info("开始同步" + page.getNumberOfElements() + "认证信息到 es");

        page.forEach(this::onAuthenticationSuccess);

    }

    public void onAuthenticationSuccess(AuthenticationInfo info) {
        try {

            info.setSyncStatus(ExecuteStatus.Failure.getValue());

            String index = getIndexString(info);

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

    public String getIndexString(AuthenticationInfo info) {
        return DEFAULT_ES_INDEX + "-" + info.getUserId() + "-" + info.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
