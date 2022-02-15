package com.github.dactiv.basic.commons.id;

import com.github.dactiv.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 带名称的 id 实体
 *
 * @author maurice.chen
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdName extends IdEntity<Integer> {

    private static final long serialVersionUID = -4127420686530448230L;

    /**
     * 名称
     */
    private String name;
}
