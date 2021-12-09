package com.github.dactiv.basic.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * <p>系统用户实体类</p>
 * <p>Table: tb_console_user - 系统用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@NoArgsConstructor
@Alias("consoleUser")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_console_user", autoResultMap = true)
public class ConsoleUserEntity extends SystemUserEntity {

    private static final long serialVersionUID = 542256170672538050L;
    /**
     * 创建 socket 事件名称
     */
    public static final String CREATE_SOCKET_EVENT_NAME = "create_console_user";
    /**
     * 更新 socket 事件名称
     */
    public static final String UPDATE_SOCKET_EVENT_NAME = "update_console_user";
    /**
     * 删除 socket 事件名称
     */
    public static final String DELETE_SOCKET_EVENT_NAME = "delete_console_user";

    /**
     * 真实姓名
     */
    @NotEmpty
    @Length(max = 16)
    private String realName;

    /**
     * 备注
     */
    private String remark;
}