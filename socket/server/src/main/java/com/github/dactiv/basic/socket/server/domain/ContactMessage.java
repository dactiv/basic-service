package com.github.dactiv.basic.socket.server.domain;

import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.MessageTypeEnum;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人消息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContactMessage<T extends BasicMessageMeta.Message> extends BasicMessageMeta implements NumberIdEntity<Integer> {
    private static final long serialVersionUID = 6725391155534568648L;

    public static final String TARGET_ID_FIELD = "targetId";

    /**
     * 主键 id 或 联系人 id（A 用户 对应有 B 联系人的话。该值为 B 的 id）
     */
    private Integer id;

    /**
     * 联系人类型
     */
    private MessageTypeEnum type;

    /**
     * 目标 id
     */
    private Integer targetId;

    /**
     * 消息内容
     */
    private List<T> messages = new ArrayList<>();
}
