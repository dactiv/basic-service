package com.github.dactiv.basic.message.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 含标题的消息实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TitleMessage extends BasicMessage {

    private static final long serialVersionUID = 969466005283913384L;

    /**
     * 标题
     */
    @NotNull
    private String title;
}
