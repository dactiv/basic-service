package com.github.dactiv.basic.message.entity;

import java.util.Date;

import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import lombok.*;

import org.apache.ibatis.type.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * <p>批量消息实体类</p>
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Alias("batchMessage")
@TableName("tb_batch_message")
public class BatchMessage {

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
     * 完成时间
     */
    private Date completeTime;

    /**
     * 状态:0.执行中、1.执行成功，99.执行失败
     */
    private Integer status = ExecuteStatus.Processing.getValue();

    /**
     * 总数
     */
    private Integer count;

    /**
     * 成功发送数量
     */
    private Integer successNumber;

    /**
     * 失败发送数量
     */
    private Integer failNumber;

    /**
     * 发送中的数量
     */
    private Integer sendingNumber;

    /**
     * 类型:10.站内信,20.邮件,30.短信
     */
    private Integer type;

    /**
     * 批量消息接口，用于统一规范使用
     *
     * @author maurice.chen
     */
    public interface Body {

        /**
         * 获取批量消息 id
         *
         * @return 批量消息 id
         */
        Integer getBatchId();

        /**
         * 设置批量消息 id
         *
         * @param batchId 批量消息 id
         */
        void setBatchId(Integer batchId);

        /**
         * 获取状态
         *
         * @return 0.执行中、1.执行成功，99.执行失败
         */
        Integer getStatus();
    }

}