package com.fuyu.basic.message.service;

import com.fuyu.basic.commons.enumerate.support.ExecuteStatus;
import com.fuyu.basic.commons.enumerate.support.YesOrNo;
import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.page.Page;
import com.fuyu.basic.commons.page.PageRequest;
import com.fuyu.basic.message.dao.EmailMessageDao;
import com.fuyu.basic.message.dao.SiteMessageDao;
import com.fuyu.basic.message.dao.SmsMessageDao;
import com.fuyu.basic.message.dao.entity.EmailMessage;
import com.fuyu.basic.message.dao.entity.SiteMessage;
import com.fuyu.basic.message.dao.entity.SmsMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    /**
     * 检查结果集并返回一条数据
     *
     * @param result 结果集
     * @param <T>    结果集类型
     * @return 单个记录
     */
    private <T> T checkResultAndReturnOne(List<T> result) {

        if (result.size() > 1) {
            throw new ServiceException("通过条件查询出来的记录存在" + result.size() + "条,并非单一记录");
        }

        Iterator<T> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

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
        emailMessageDao.update(emailMessage);
    }

    /**
     * 删除邮件消息
     *
     * @param id 主键 id
     */
    public void deleteEmailMessage(Integer id) {
        emailMessageDao.delete(id);
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
        return emailMessageDao.get(id);
    }

    /**
     * 获取邮件消息
     *
     * @param filter 过滤条件
     * @return 邮件消息
     */
    public EmailMessage getEmailMessageByFilter(Map<String, Object> filter) {

        List<EmailMessage> result = findEmailMessageList(filter);

        return checkResultAndReturnOne(result);
    }

    /**
     * 根据过滤条件查找邮件消息数据
     *
     * @param filter 过滤条件
     * @return 邮件消息实体集合
     */
    public List<EmailMessage> findEmailMessageList(Map<String, Object> filter) {
        return emailMessageDao.find(filter);
    }

    /**
     * 查找邮件消息分页数据
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<EmailMessage> findEmailMessagePage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<EmailMessage> data = findEmailMessageList(filter);

        return new Page<>(pageRequest, data);
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
        siteMessageDao.update(siteMessage);
    }

    /**
     * 删除站内信消息
     *
     * @param id 主键 id
     */
    public void deleteSiteMessage(Integer id) {
        siteMessageDao.delete(id);
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
        return siteMessageDao.get(id);
    }

    /**
     * 获取站内信消息
     *
     * @param filter 过滤条件
     * @return 站内信消息
     */
    public SiteMessage getSiteMessageByFilter(Map<String, Object> filter) {

        List<SiteMessage> result = findSiteMessageList(filter);

        return checkResultAndReturnOne(result);
    }

    /**
     * 根据过滤条件查找站内信消息数据
     *
     * @param filter 过滤条件
     * @return 站内信消息实体集合
     */
    public List<SiteMessage> findSiteMessageList(Map<String, Object> filter) {
        return siteMessageDao.find(filter);
    }

    /**
     * 查找站内信消息分页数据
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<SiteMessage> findSiteMessagePage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<SiteMessage> data = findSiteMessageList(filter);

        return new Page<>(pageRequest, data);
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

        Map<String, Object> filter = new LinkedHashMap<>();

        if (CollectionUtils.isNotEmpty(types)) {
            filter.put("typeContain", types);
        }

        filter.put("toUserIdEq", userId);

        List<SiteMessage> siteMessages = findSiteMessageList(filter);

        siteMessages.forEach(s -> {

            s.setIsRead(YesOrNo.Yes.getValue());
            s.setReadTime(new Date());

            saveSiteMessage(s);

        });
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
        smsMessageDao.update(smsMessage);
    }

    /**
     * 删除短信消息
     *
     * @param id 主键 id
     */
    public void deleteSmsMessage(Integer id) {
        smsMessageDao.delete(id);
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
        return smsMessageDao.get(id);
    }

    /**
     * 获取短信消息
     *
     * @param filter 过滤条件
     * @return 短信消息
     */
    public SmsMessage getSmsMessageByFilter(Map<String, Object> filter) {

        List<SmsMessage> result = findSmsMessageList(filter);

        return checkResultAndReturnOne(result);
    }

    /**
     * 根据过滤条件查找短信消息数据
     *
     * @param filter 过滤条件
     * @return 短信消息实体集合
     */
    public List<SmsMessage> findSmsMessageList(Map<String, Object> filter) {
        return smsMessageDao.find(filter);
    }

    /**
     * 查找短信消息分页数据
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<SmsMessage> findSmsMessagePage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<SmsMessage> data = findSmsMessageList(filter);

        return new Page<>(pageRequest, data);
    }
}
