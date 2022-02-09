package com.github.dactiv.basic.authentication.domain.meta;

import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 * 后台用户部门原数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(staticName = "of")
public class ConsoleUserDepartmentMeta extends IntegerIdEntity {

    private static final long serialVersionUID = 7003419898265010307L;

    /**
     * 主键 id
     */
    private String name;
}
