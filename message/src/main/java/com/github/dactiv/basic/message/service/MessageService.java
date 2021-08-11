package com.github.dactiv.basic.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.github.dactiv.basic.message.dao.EmailMessageDao;
import com.github.dactiv.basic.message.dao.SiteMessageDao;
import com.github.dactiv.basic.message.dao.SmsMessageDao;
import com.github.dactiv.basic.message.dao.entity.EmailMessage;
import com.github.dactiv.basic.message.dao.entity.SiteMessage;
import com.github.dactiv.basic.message.dao.entity.SmsMessage;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 消息管理服务
 *
 * @author maurice
 * @since 2020-04-06 09:32:18
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MessageService {

    @Autowired
    private EmailMessageDao emailMessageDao;

    @Autowired
    private SiteMessageDao siteMessageDao;

    @Autowired
    private SmsMessageDao smsMessageDao;

    // ----------------------------- 邮件消息管理 ----------------------------- //

    public void saveEmailMessages(List<EmailMessage> emailMessages) {
        emailMessages.forEach(this::saveEmailMessage);
    }

    /**
     * 保存邮件消息
     *
     * @param emailMessage 邮件消息实体
     */
    public void saveEmailMessage(EmailMessage emailMessage) {
        if (Objects.isNull(emailMessage.getId())) {
            insertEmailMessage(emailMessage);
        } else {
            updateEmailMessage(emailMessage);
        }
    }

    /**
     * 更新邮件消息
     *
     * @param emailMessage 邮件消息实体
     */
    public void insertEmailMessage(EmailMessage emailMessage) {

        emailMessage.setRetryCount(0);
        emailMessage.setStatus(ExecuteStatus.Processing.getValue());

        emailMessageDao.insert(emailMessage);
    }

    /**
     * 更新邮件消息
     *
     * @param emailMessage 邮件消息实体
     */
    public void updateEmailMessage(EmailMessage emailMessage) {
        emailMessageDao.updateById(emailMessage);
    }

    /**
     * 删除邮件消息
     *
     * @param id 主键 id
     */
    public void deleteEmailMessage(Integer id) {
        emailMessageDao.deleteById(id);
    }

    /**
     * 删除邮件消息
     *
     * @param ids 主键 id 集合
     */
    public void deleteEmailMessage(List<Integer> ids) {
        for (Integer id : ids) {
            deleteEmailMessage(id);
        }
    }

    /**
     * 获取邮件消息
     *
     * @param id 主键 id
     * @return 邮件消息实体
     */
    public EmailMessage getEmailMessage(Integer id) {
        return emailMessageDao.selectById(id);
    }

    /**
     * 查找邮件消息分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper     过滤条件
     * @return 分页实体
     */
    public Page<EmailMessage> findEmailMessagePage(PageRequest pageRequest, Wrapper<EmailMessage> wrapper) {

        PageDto<EmailMessage> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<EmailMessage> result = emailMessageDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    // ----------------------------- 站内信消息管理 ----------------------------- //

    public void saveSiteMessages(List<SiteMessage> siteMessages) {
        siteMessages.forEach(this::saveSiteMessage);
    }

    /**
     * 保存站内信消息
     *
     * @param siteMessage 站内信消息实体
     */
    public void saveSiteMessage(SiteMessage siteMessage) {
        if (Objects.isNull(siteMessage.getId())) {
            insertSiteMessage(siteMessage);
        } else {
            updateSiteMessage(siteMessage);
        }
    }

    /**
     * 更新站内信消息
     *
     * @param siteMessage 站内信消息实体
     */
    public void insertSiteMessage(SiteMessage siteMessage) {
        siteMessageDao.insert(siteMessage);
    }

    /**
     * 更新站内信消息
     *
     * @param siteMessage 站内信消息实体
     */
    public void updateSiteMessage(SiteMessage siteMessage) {
        siteMessageDao.updateById(siteMessage);
    }

    /**
     * 删除站内信消息
     *
     * @param id 主键 id
     */
    public void deleteSiteMessage(Integer id) {
        siteMessageDao.deleteById(id);
    }

    /**
     * 删除站内信消息
     *
     * @param ids 主键 id 集合
     */
    public void deleteSiteMessage(List<Integer> ids) {
        for (Integer id : ids) {
            deleteSiteMessage(id);
        }
    }

    /**
     * 获取站内信消息
     *
     * @param id 主键 id
     * @return 站内信消息实体
     */
    public SiteMessage getSiteMessage(Integer id) {
        return siteMessageDao.selectById(id);
    }

    /**
     * 根据过滤条件查找站内信消息数据
     *
     * @param wrapper 包装器
     * @return 站内信消息实体集合
     */
    public List<SiteMessage> findSiteMessageList(Wrapper<SiteMessage> wrapper) {
        return siteMessageDao.selectList(wrapper);
    }

    /**
     * 查找站内信消息分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper     包装器
     * @return 分页实体
     */
    public Page<SiteMessage> findSiteMessagePage(PageRequest pageRequest, Wrapper<SiteMessage> wrapper) {
        PageDto<SiteMessage> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<SiteMessage> result = siteMessageDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 计数站内信未读数量
     *
     * @param userId 用户 id
     * @return 按类型分组的未读数量
     */
    public List<Map<String, Object>> countSiteMessageUnreadQuantity(Integer userId) {
        return siteMessageDao.countUnreadQuantity(userId);
    }

    /**
     * 阅读站内信
     *
     * @param types  站内信类型集合
     * @param userId 用户 id
     */
    public void readSiteMessage(List<String> types, Integer userId) {

        LambdaQueryWrapper<SiteMessage> queryWrapper = new LambdaQueryWrapper<>();

        Map<String, Object> filter = new LinkedHashMap<>();

        if (CollectionUtils.isNotEmpty(types)) {
            queryWrapper.in(SiteMessage::getType, types);
        }

        queryWrapper.eq(SiteMessage::getToUserId, userId);

        List<SiteMessage> siteMessages = findSiteMessageList(queryWrapper);

        siteMessages
                .stream()
                .peek(s -> s.setIsRead(YesOrNo.Yes.getValue()))
                .peek(s -> s.setReadTime(new Date()))
                .forEach(this::saveSiteMessage);
    }

    // ----------------------------- 短信消息管理 ----------------------------- //

    public void saveSmsMessages(List<SmsMessage> smsMessages) {
        smsMessages.forEach(this::saveSmsMessage);
    }

    /**
     * 保存短信消息
     *
     * @param smsMessage 短信消息实体
     */
    public void saveSmsMessage(SmsMessage smsMessage) {
        if (Objects.isNull(smsMessage.getId())) {
            insertSmsMessage(smsMessage);
        } else {
            updateSmsMessage(smsMessage);
        }
    }

    /**
     * 更新短信消息
     *
     * @param smsMessage 短信消息实体
     */
    public void insertSmsMessage(SmsMessage smsMessage) {

        smsMessage.setRetryCount(0);
        smsMessage.setStatus(ExecuteStatus.Processing.getValue());

        smsMessageDao.insert(smsMessage);
    }

    /**
     * 更新短信消息
     *
     * @param smsMessage 短信消息实体
     */
    public void updateSmsMessage(SmsMessage smsMessage) {
        smsMessageDao.updateById(smsMessage);
    }

    /**
     * 删除短信消息
     *
     * @param id 主键 id
     */
    public void deleteSmsMessage(Integer id) {
        smsMessageDao.deleteById(id);
    }

    /**
     * 删除短信消息
     *
     * @param ids 主键 id 集合
     */
    public void deleteSmsMessage(List<Integer> ids) {
        for (Integer id : ids) {
            deleteSmsMessage(id);
        }
    }

    /**
     * 获取短信消息
     *
     * @param id 主键 id
     * @return 短信消息实体
     */
    public SmsMessage getSmsMessage(Integer id) {
        return smsMessageDao.selectById(id);
    }

    /**
     * 查找短信消息分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper     包装器
     * @return 分页实体
     */
    public Page<SmsMessage> findSmsMessagePage(PageRequest pageRequest, Wrapper<SmsMessage> wrapper) {
        PageDto<SmsMessage> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<SmsMessage> result = smsMessageDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }
}
