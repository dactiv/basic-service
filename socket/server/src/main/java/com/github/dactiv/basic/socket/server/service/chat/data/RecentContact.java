package com.github.dactiv.basic.socket.server.service.chat.data;

import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 常用联系人实体，用于记录常用联系人的基本信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RecentContact extends IntegerIdEntity {
    private static final long serialVersionUID = 7454008176314089935L;
    /**
     * 类型
     *
     * @see com.github.dactiv.basic.socket.server.enumerate.ContactType
     */
    private Integer type;
}
