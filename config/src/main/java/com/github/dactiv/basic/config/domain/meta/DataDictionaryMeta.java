package com.github.dactiv.basic.config.domain.meta;

import com.github.dactiv.basic.config.enumerate.ValueTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 数据字典元数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class DataDictionaryMeta implements Serializable {

    private static final long serialVersionUID = -6880817354929730676L;

    /**
     * 名称
     */
    @NotEmpty
    @Length(max = 64)
    private String name;

    /**
     * 值
     */
    @NotEmpty
    private Object value;

    /**
     * 值类型
     */
    @NotNull
    private ValueTypeEnum valueType;

    /**
     * 等级
     */
    private String level;
}
