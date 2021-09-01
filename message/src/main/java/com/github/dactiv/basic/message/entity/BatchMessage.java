package com.github.dactiv.basic.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.basic.message.enumerate.AttachmentType;
import com.github.dactiv.framework.commons.enumerate.NameValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;


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
    private Integer count = 0;

    /**
     * 成功发送数量
     */
    private Integer successNumber = 0;

    /**
     * 失败发送数量
     */
    private Integer failNumber = 0;

    /**
     * 发送中的数量
     */
    private Integer sendingNumber = 0;

    /**
     * 类型:10.站内信,20.邮件,30.短信
     */
    private Integer type;

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTypeName() {
        return NameValueEnumUtils.getName(this.type, AttachmentType.class);
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        return NameValueEnumUtils.getName(this.status, ExecuteStatus.class);
    }

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