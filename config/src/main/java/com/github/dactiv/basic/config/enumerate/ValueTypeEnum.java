package com.github.dactiv.basic.config.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.*;

/**
 * 数据字典值类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ValueTypeEnum implements NameValueEnum<Integer> {

    /**
     * 整数类型
     */
    INTEGER(10, "整数类型", Integer.class),

    /**
     * 整数类型
     */
    DOUBLE(20, "浮点类型", Double.class),

    /**
     * 字符串类型
     */
    STRING(30, "字符串类型", String.class),

    /**
     * 日期类型
     */
    DATE(40, "日期类型", LocalDate.class),

    /**
     * 时期和时间类型
     */
    DATE_TIME(50, "时期和时间类型", LocalDateTime.class),

    /**
     * 时间类型
     */
    TIME(60, "时间类型", LocalTime.class),

    /**
     * 年份类型
     */
    YEAR(70, "年份类型", Year.class),

    /**
     * 年月类型
     */
    YEAR_MONTH(80, "年月类型", YearMonth.class),

    /**
     * 长整型
     */
    LONG(90, "长整型", Long.class),

    ;

    private final Integer value;

    private final String name;

    private final Class<?> classType;

}
