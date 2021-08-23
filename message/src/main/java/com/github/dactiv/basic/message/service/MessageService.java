package com.github.dactiv.basic.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.github.dactiv.basic.message.dao.*;
import com.github.dactiv.basic.message.entity.BatchMessage;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.entity.SiteMessage;
import com.github.dactiv.basic.message.entity.SmsMessage;
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
    private BatchMessageDao batchMessageDao;

    @Autowired
    private SmsMessageDao smsMessageDao;

    // ----------------------------- 批量消息管理 ----------------------------- //

    /**
     * 保存 table : tb_batch_message 实体
     *
     * @param batchMessage 实体
     * @see BatchMessage
     */
    public void saveBatchMessage(BatchMessage batchMessage) {
        if (Objects.isNull(batchMessage.getId())) {
            insertBatchMessage(batchMessage);
        } else {
            updateBatchMessage(batchMessage);
        }
    }

    /**
     * 新增 table : tb_batch_message 实体
     *
     * @param batchMessage 实体
     * @see BatchMessage
     */
    public void insertBatchMessage(BatchMessage batchMessage) {
        batchMessageDao.insert(batchMessage);
    }

    /**
     * 更新 table : tb_batch_message 实体
     *
     * @param batchMessage 实体
     * @see BatchMessage
     */
    public void updateBatchMessage(BatchMessage batchMessage) {
        batchMessageDao.updateById(batchMessage);
    }

    /**
     * 删除 table : tb_batch_message 实体
     *
     * @param id 主键 id
     * @see BatchMessage
     */
    public void deleteBatchMessage(Integer id) {
        batchMessageDao.deleteById(id);
    }

    /**
     * 删除 table : tb_batch_message 实体
     *
     * @param ids 主键 id 集合
     * @see BatchMessage
     */
    public void deleteBatchMessage(List<Integer> ids) {
        ids.forEach(this::deleteBatchMessage);
    }

    /**
     * 获取 table : tb_batch_message 实体
     *
     * @param id 主键 id
     * @return tb_batch_message 实体
     * @see BatchMessage
     */
    public BatchMessage getBatchMessage(Integer id) {
        return batchMessageDao.selectById(id);
    }

    /**
     * 获取 table : tb_batch_message 实体
     *
     * @param wrapper 过滤条件
     * @return tb_batch_message 实体
     * @see BatchMessage
     */
    public BatchMessage findOneBatchMessage(Wrapper<BatchMessage> wrapper) {
        return batchMessageDao.selectOne(wrapper);
    }

    /**
     * 根据过滤条件查找 table : tb_batch_message 实体
     *
     * @param wrapper 过滤条件
     * @return tb_batch_message 实体集合
     * @see BatchMessage
     */
    public List<BatchMessage> findBatchMessageList(Wrapper<BatchMessage> wrapper) {
        return batchMessageDao.selectList(wrapper);
    }

    /**
     * 查找 table : tb_batch_message 实体分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper     过滤条件
     * @return 分页实体
     * @see BatchMessage
     */
    public Page<BatchMessage> findBatchMessagePage(PageRequest pageRequest, Wrapper<BatchMessage> wrapper) {

        IPage<BatchMessage> result = batchMessageDao.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageRequest),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    // ----------------------------- 短信消息管理 ----------------------------- //

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
