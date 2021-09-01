package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

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
@Alias("attachment")
@TableName(value = "tb_attachment", autoResultMap = true)
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
     * 附件类型，所属什么消息使用的附件（10.站内信，20:邮件）
     *
     * @see com.github.dactiv.basic.message.enumerate.AttachmentType
     */
    @NotNull
    private Integer type;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * 附件类型
     */
    @NotNull
    private String contentType;

    /**
     * 对应的消息主键
     */
    private Integer messageId;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> meta;
}
