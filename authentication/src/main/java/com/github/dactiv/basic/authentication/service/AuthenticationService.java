package com.github.dactiv.basic.authentication.service;

import com.github.dactiv.basic.authentication.dao.AuthenticationInfoDao;
import com.github.dactiv.basic.authentication.dao.entity.AuthenticationInfo;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.spring.web.RestResult;
import com.github.dactiv.framework.spring.security.concurrent.annotation.ConcurrentProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 认证信息表管理服务
 *
 * @author maurice
 * @since 2020-06-01 08:20:59
 */
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
        authenticationInfoDao.update(authenticationInfo);
    }

    /**
     * 删除认证信息表实体
     *
     * @param id 主键 id
     */
    public void deleteAuthenticationInfo(Integer id) {
        authenticationInfoDao.delete(id);
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
     * @return 认证信息表实体
     */
    public AuthenticationInfo getAuthenticationInfo(Integer id) {
        return authenticationInfoDao.get(id);
    }

    /**
     * 获取认证信息表实体
     *
     * @param filter 过滤条件
     * @return 认证信息表实体
     */
    public AuthenticationInfo getAuthenticationInfoByFilter(Map<String, Object> filter) {

        List<AuthenticationInfo> result = findAuthenticationInfoList(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录大于" + result.size() + "条,并非单一记录");
        }

        Iterator<AuthenticationInfo> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 根据过滤条件查找认证信息表实体
     *
     * @param filter 过滤条件
     * @return 认证信息表实体集合
     */
    public List<AuthenticationInfo> findAuthenticationInfoList(Map<String, Object> filter) {
        return authenticationInfoDao.find(filter);
    }

    /**
     * 查找认证信息表实体分页数据
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<AuthenticationInfo> findAuthenticationInfoPage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<AuthenticationInfo> data = findAuthenticationInfoList(filter);

        return new Page<>(pageRequest, data);
    }

    public void validAuthenticationInfo(AuthenticationInfo info) {

        PageRequest pageRequest = new PageRequest(0, 1);

        Map<String, Object> filter = new LinkedHashMap<>(pageRequest.getOffsetMap());

        filter.put("userIdEq", info.getUserId());
        filter.put("typeContain", Collections.singletonList(info.getType()));
        filter.put("idNeq", info.getId());

        AuthenticationInfo authenticationInfo = getAuthenticationInfoByFilter(filter);

        if (authenticationInfo != null && !authenticationInfo.equals(info) && info.getId() > authenticationInfo.getId()) {

            Map<String, Object> param = new LinkedHashMap<>();

            param.put(MessageService.DEFAULT_MESSAGE_TYPE_KEY, MessageService.DEFAULT_MESSAGE_TYPE_VALUE);

            param.put("content", sendContent);
            param.put("fromUserId", fromUserId);
            param.put("toUserId", info.getUserId());
            param.put("type", messageType);
            param.put("title", title);
            param.put("link", info.idEntityToMap());
            param.put("data", info.toMap());
            param.put("pushMessage", YesOrNo.Yes.getValue());

            RestResult<Map<String, Object>> result = messageService.sendMessage(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }

        }
    }

    @ConcurrentProcess(value = "sync.authentication.info", exceptionMessage = "同步认证信息遇到并发，不执行重试操作")
    @Scheduled(cron = "${dynamic.retry.cron.expression:0 0/3 * * * ? }")
    public void syncAuthenticationInfo() {

        PageRequest pageRequest = new PageRequest(0, 100);

        Map<String, Object> filter = pageRequest.getOffsetMap();

        filter.put("retryCountLeq", maxRetryCount);
        filter.put("syncStatusNeq", ExecuteStatus.Success.getValue());

        List<AuthenticationInfo> authenticationInfos = findAuthenticationInfoList(filter);

        LOGGER.info("开始同步" + authenticationInfos.size() + "认证信息到 es");

        authenticationInfos.forEach(this::onAuthenticationSuccess);

    }

    public void onAuthenticationSuccess(AuthenticationInfo info) {
        try {

            info.setSyncStatus(ExecuteStatus.Failure.getValue());

            String index = getIndexString(info);

            IndexOperations indexOperations =elasticsearchRestTemplate.indexOps(IndexCoordinates.of(index));

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
        Instant instant = info.getCreationTime().toInstant();
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        return DEFAULT_ES_INDEX + "-" + info.getUserId() + "-" + time.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
