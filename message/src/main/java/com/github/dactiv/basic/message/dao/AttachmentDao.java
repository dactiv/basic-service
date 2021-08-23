package com.github.dactiv.basic.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.message.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_attachment 的数据访问
 *
 * <p>Table: tb_attachment - 消息附件</p>
 *
 * @see Attachment
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface AttachmentDao extends BaseMapper<Attachment> {
}
