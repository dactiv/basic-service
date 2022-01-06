package com.github.dactiv.basic.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.message.dao.SiteMessageDao;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.basic.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.basic.message.enumerate.AttachmentTypeEnum;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * tb_site_message 的业务逻辑
 *
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice.chen
 * @see SiteMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SiteMessageService extends BasicService<SiteMessageDao, SiteMessageEntity> {

    private final AttachmentService attachmentService;

    public SiteMessageService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @Override
    public SiteMessageEntity get(Serializable id) {
        SiteMessageEntity result = super.get(id);

        if (YesOrNo.Yes.equals(result.getHasAttachment())) {
            List<AttachmentEntity> attachmentList = attachmentService
                    .lambdaQuery()
                    .eq(AttachmentEntity::getMessageId, result.getId())
                    .eq(AttachmentEntity::getType, AttachmentTypeEnum.Email.getValue())
                    .list();
            result.setAttachmentList(attachmentList);
        }

        return result;
    }

    /**
     * 计数站内信未读数量
     *
     * @param userId 用户 id
     *
     * @return 按类型分组的未读数量
     */
    public List<Map<String, Object>> countUnreadQuantity(Integer userId) {
        return getBaseMapper().countUnreadQuantity(userId);
    }

    /**
     * 阅读站内信
     *
     * @param types  站内信类型集合
     * @param userId 用户 id
     */
    public void read(List<String> types, Integer userId) {

        LambdaQueryWrapper<SiteMessageEntity> queryWrapper = Wrappers
                .<SiteMessageEntity>lambdaQuery()
                .select(BasicMessageEntity::getId)
                .eq(SiteMessageEntity::getToUserId, userId)
                .eq(SiteMessageEntity::getIsRead, YesOrNo.No.getValue());

        if (CollectionUtils.isNotEmpty(types)) {
            queryWrapper.in(SiteMessageEntity::getType, types);
        }

        findObjects(queryWrapper, Integer.class)
                .stream()
                .map(SiteMessageEntity::new)
                .peek(s -> s.setIsRead(YesOrNo.Yes))
                .peek(s -> s.setReadTime(new Date()))
                .forEach(this::save);
    }
}
