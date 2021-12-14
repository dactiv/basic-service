package com.github.dactiv.basic.socket.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.basic.socket.server.enumerate.ContactTypeEnum;
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

    /**
     * 主键 id 或 联系人 id（A 用户 对应有 B 联系人的话。该值为 B 的 id）
     */
    private Integer id;

    /**
     * 联系人类型
     *
     * @see ContactTypeEnum
     */
    private Integer type;

    /**
     * 目标用户 id
     */
    @JsonIgnore
    private Integer targetId;

    /**
     * 消息内容
     */
    private List<T> messages = new ArrayList<>();
}