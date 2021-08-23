package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * <p>消息附件实体类</p>
 * <p>Table: tb_attachment - 消息附件</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@Alias("emailMessage")
@TableName("tb_email_attachment")
public class Attachment implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 9190301660565089712L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 类型
     *
     * @see com.github.dactiv.basic.message.enumerate.AttachmentType
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 连接
     */
    private String link;

    /**
     * 后缀
     */
    private String suffix;

    /**
     * 对应的消息主键
     */
    private Integer messageId;
}
