package com.github.dactiv.basic.authentication.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>认证信息实体类</p>
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice
 * @since 2020-06-01 09:22:12
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@TableName("tb_authentication_info")
public class AuthenticationInfo implements Serializable {

    public static final String DEFAULT_INDEX = "authentication-info";

    private static final long serialVersionUID = -5548079224380108843L;
    /**
     * 主键
     */
    @Id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 版本号
     */
    @Version
    @JsonIgnore
    private Integer updateVersion = 1;

    /**
     * 用户 id
     */
    @NotNull
    private Integer userId;

    /**
     * 用户类型
     */
    @NotEmpty
    private String type;

    /**
     * ip 地址
     */
    @NotEmpty
    private String ip;

    /**
     * 设备名称
     */
    @NotEmpty
    private String device;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String area;

    /**
     * 同步 es 状态：0.处理中，1.成功，99.失败
     */
    @EqualsAndHashCode.Exclude
    private Integer syncStatus = ExecuteStatus.Processing.getValue();

    /**
     * 重试次数
     */
    @EqualsAndHashCode.Exclude
    private Integer retryCount = 0;

    /**
     * 备注
     */
    @EqualsAndHashCode.Exclude
    private String remark;
}