package com.github.dactiv.basic.commons.id;

import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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

    public static IdName of(Integer id, String name) {
        IdName result = new IdName();
        result.setId(id);
        result.setName(name);
        return result;
    }

    public static IdName of(BasicIdentification<Integer> source) {
        return of(source, NameEnum.FIELD_NAME);
    }

    public static IdName of(BasicIdentification<Integer> source, String nameProperty) {
        IdName result = new IdName();
        result.setId(source.getId());

        Object nameValue = ReflectionUtils.getReadProperty(source, nameProperty);

        if (Objects.isNull(nameValue)) {
            nameValue = ReflectionUtils.getFieldValue(source, nameProperty);
        }

        if (Objects.nonNull(nameValue)) {
            result.setName(nameValue.toString());
        }

        return result;
    }
}
