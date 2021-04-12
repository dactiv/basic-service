package com.fuyu.basic.commons.enumerate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.exception.NameEnumNotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 带有名称的枚举工具类
 *
 * @author maurice.chen
 */
public class NameEnumUtils {

    /**
     * 获取带有名称的枚举 name 值
     *
     * @param value     枚举值
     * @param enumClass 枚举类型
     * @return name 值
     */
    public static String getName(String value, Class<? extends Enum<? extends NameEnum>> enumClass) {
        return getName(value, enumClass, false);
    }

    /**
     * 获取带有名称的枚举 name 值
     *
     * @param value          枚举值
     * @param enumClass      枚举类型
     * @param ignoreNotFound 如果找不到是否抛出异常, true:抛出，否则 false
     * @return name 值
     */
    public static String getName(String value, Class<? extends Enum<? extends NameEnum>> enumClass, boolean ignoreNotFound) {
        Enum<? extends NameEnum>[] values = enumClass.getEnumConstants();

        for (Enum<? extends NameEnum> anEnum : values) {
            NameEnum nameEnum = (NameEnum) anEnum;
            if (anEnum.name().equals(value)) {
                return nameEnum.getName();
            }
        }

        if (!ignoreNotFound) {
            String msg = enumClass.getName() + " 中找不到值为: " + value + " 的对应名称，" +
                    enumClass.getName() + "信息为:" + castMap(enumClass);
            throw new NameEnumNotFoundException(msg);
        }

        return null;
    }

    /**
     * 将带有名称的枚举类转型为 map
     *
     * @param enumClass 带有名称的枚举
     * @return map， key 为枚举的 name，value 为枚举的 getName()
     */
    public static Map<String, Object> castMap(Class<? extends Enum<? extends NameEnum>> enumClass) {
        return castMap(enumClass, (String[]) null);
    }

    /**
     * 将带有名称的枚举类转型为 map
     *
     * @param enumClass 带有名称的枚举
     * @param ignore    要过滤的值（枚举的 name() 值）
     * @return map， key 为枚举的 name()，value 为枚举的 getName()
     */
    public static Map<String, Object> castMap(Class<? extends Enum<? extends NameEnum>> enumClass, String... ignore) {
        Map<String, Object> result = new LinkedHashMap<>();
        Enum<? extends NameEnum>[] values = enumClass.getEnumConstants();

        if (ArrayUtils.isEmpty(values)) {
            return result;
        }

        List<String> ignoreList = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(ignore)) {
            ignoreList = Arrays.asList(ignore);
        }

        List<String> jsonIgnoreList = getJsonIgnoreList(enumClass);

        for (Enum<? extends NameEnum> o : values) {

            NameEnum ve = (NameEnum) o;

            if (jsonIgnoreList.contains(o.toString())) {
                continue;
            }

            String value = ve.getName();

            if (ignoreList.contains(o.name())) {
                continue;
            }

            result.put(o.name(), value);
        }

        return result;
    }

    /**
     * 将值转型为枚举类
     *
     * @param value     枚举值
     * @param enumClass 带有名称的枚举
     * @param <T>       带有名称的枚举接口子类
     * @return 带有名称的枚举接口实现类
     */
    public static <T extends Enum<? extends NameEnum>> T parse(String value, Class<T> enumClass) {
        return parse(value, enumClass, false);
    }

    /**
     * 将值转型为枚举类
     *
     * @param value          枚举值
     * @param enumClass      带有名称的枚举
     * @param ignoreNotFound 如果找不到是否抛出异常, true:抛出，否则 false
     * @param <T>            带有名称的枚举接口子类
     * @return 带有名称的枚举接口实现类
     */
    public static <T extends Enum<? extends NameEnum>> T parse(String value, Class<T> enumClass, boolean ignoreNotFound) {
        Enum<? extends NameEnum>[] values = enumClass.getEnumConstants();

        for (Enum<? extends NameEnum> o : values) {
            if (Objects.equals(o.name(), value)) {
                return Casts.cast(o);
            }
        }

        if (!ignoreNotFound) {
            String msg = enumClass.getName() + " 中找不到值为: " + value + " 的对应名称，" +
                    enumClass.getName() + "信息为:" + castMap(enumClass);
            throw new NameEnumNotFoundException(msg);
        }

        return null;
    }

    public static List<String> getJsonIgnoreList(Class<?> enumClass) {

        List<String> ignoreList = new ArrayList<>();

        JsonIgnoreProperties jsonIgnoreProperties =  AnnotationUtils.findAnnotation(enumClass,JsonIgnoreProperties.class);

        if (jsonIgnoreProperties != null) {
            ignoreList.addAll(Arrays.asList(jsonIgnoreProperties.value()));
        }

        Field[] fieldList = enumClass.getFields();

        for (Field field : fieldList) {
            JsonIgnore jsonIgnore = AnnotationUtils.findAnnotation(field,JsonIgnore.class);
            if (jsonIgnore != null) {
                ignoreList.add(field.getName());
            }
        }

        return ignoreList;
    }

}
