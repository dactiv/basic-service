package com.fuyu.basic.authentication.controller;


import com.fuyu.basic.authentication.dao.entity.MemberUser;
import com.fuyu.basic.authentication.service.UserService;
import com.fuyu.basic.authentication.service.security.MobileUserDetailsService;
import com.fuyu.basic.authentication.service.security.handler.JsonLogoutSuccessHandler;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.page.Page;
import com.fuyu.basic.commons.page.PageRequest;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.support.security.audit.AuditEventEntity;
import com.fuyu.basic.support.security.audit.Auditable;
import com.fuyu.basic.support.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import com.fuyu.basic.support.security.entity.MobileUserDetails;
import com.fuyu.basic.support.security.authentication.token.PrincipalAuthenticationToken;
import com.fuyu.basic.support.security.entity.SecurityUserDetails;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import com.fuyu.basic.support.security.enumerate.UserStatus;
import com.fuyu.basic.support.security.plugin.Plugin;
import com.fuyu.basic.support.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 授权控制器
 *
 * @author maurice.chen
 */
@RefreshScope
@RestController
public class SecurityController {

    @Autowired
    private UserService userService;

    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private MobileUserDetailsService mobileUserDetailsService;

    /**
     * 获取用户审计数据
     *
     * @param principal 用户登陆账户
     * @param startTime 数据发生时间
     * @param endTime   数据发生时间
     * @param type      审计类型
     * @return 审计事件
     */
    @PostMapping("audit")
    @Plugin(name = "审计管理", id = "audit", parent = "system", type = ResourceType.Menu, source = ResourceSource.Console)
    public Page<AuditEventEntity> audit(PageRequest pageRequest,
                                        @RequestParam(required = false) String principal,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date startTime,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date endTime,
                                        @RequestParam(required = false) String type) {

        String index = ElasticsearchAuditEventRepository.DEFAULT_ES_INDEX + "-*";

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withPageable(
                        org.springframework.data.domain.PageRequest.of(
                                pageRequest.getPageNumber() - 1,
                                pageRequest.getPageSize()
                        )
                );

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        builder.withQuery(boolQueryBuilder);

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("creationTime");
        boolQueryBuilder.must(rangeQueryBuilder);

        if (StringUtils.isNotEmpty(principal)) {
            index = ElasticsearchAuditEventRepository.DEFAULT_ES_INDEX + "-" + principal + "-*";
            boolQueryBuilder.must(QueryBuilders.termQuery("principal", principal));
        }

        if (startTime != null) {
            rangeQueryBuilder.gte(startTime);
        }

        if (endTime != null) {
            rangeQueryBuilder.lte(endTime);
        }

        if (StringUtils.isNotEmpty(type)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("type", type));
        }

        builder.withSort(SortBuilders.fieldSort("creationTime").order(SortOrder.DESC));

        SearchHits<AuditEventEntity> data = elasticsearchRestTemplate.search(builder.build(), AuditEventEntity.class, IndexCoordinates.of(index));

        return new Page<>(pageRequest, data.stream().map(SearchHit::getContent).collect(Collectors.toList()));
    }

    /**
     * 登录预处理
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @GetMapping("prepare")
    public RestResult<Map<String, Object>> prepare(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 用户登陆
     *
     * @return 未授权访问结果
     */
    @GetMapping("login")
    public RestResult<Map<String, Object>> login(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 登陆成功后跳转的连接，直接获取当前用户
     *
     * @param securityContext 安全上下文
     * @return 当前用户
     */
    @GetMapping("getPrincipal")
    @PreAuthorize("isAuthenticated()")
    public SecurityUserDetails getPrincipal(@CurrentSecurityContext SecurityContext securityContext) {
        return Casts.cast(securityContext.getAuthentication().getDetails());
    }

    /**
     * 验证移动用户明细信息
     *
     * @param deviceIdentified 唯一识别
     * @param username         登录账户
     * @param password         登录密码
     * @return 移动端的用户明细实现
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("validMobileUserDetails")
    @Auditable(type = "验证移动用户明细信息")
    public MobileUserDetails validMobileUserDetails(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                                    @RequestParam String username,
                                                    @RequestParam String password) {

        MobileUserDetails mobileUserDetails = mobileUserDetailsService.getMobileUserDetails(username);

        if (mobileUserDetails == null) {
            throw new ServiceException("找不到[" + username + "]的移动用户明细");
        }

        if (!mobileUserDetails.getDeviceIdentified().equals(deviceIdentified)) {
            throw new ServiceException("设备 ID 不匹配");
        }

        String rawPassword = DigestUtils.md5DigestAsHex((password + username + deviceIdentified).getBytes())
                + mobileUserDetails.getDevice().toString()
                + mobileUserDetails.getDevice().getDevicePlatform().name();

        if (!mobileUserDetailsService.getPasswordEncoder().matches(rawPassword, mobileUserDetails.getPassword())) {
            throw new ServiceException("用户名密码错误");
        }

        PrincipalAuthenticationToken token = new PrincipalAuthenticationToken(
                mobileUserDetails.getUsername(),
                mobileUserDetails.getPassword(),
                mobileUserDetails.getType()
        );

        token.setDetails(mobileUserDetails);

        mobileUserDetailsService.onSuccessAuthentication(token);

        return mobileUserDetails;
    }

    /**
     * 更新会员用户状态
     *
     * @param id     用户 id
     * @param status 状态值
     * @return 消息结果集
     */
    @PreAuthorize("hasRole('BASIC')")
    @PostMapping("updateMemberUserStatus")
    public RestResult.Result<?> updateMemberUserStatus(@RequestParam Integer id, @RequestParam String status) {
        MemberUser memberUser = userService.getMemberUser(id);

        memberUser.setStatus(UserStatus.valueOf(status).getValue());

        userService.updateMemberUser(memberUser);

        return RestResult.build("修改成功");
    }

}
