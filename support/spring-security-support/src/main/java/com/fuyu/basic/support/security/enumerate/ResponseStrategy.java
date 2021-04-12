package com.fuyu.basic.support.security.enumerate;

import com.fuyu.basic.commons.enumerate.NameValueEnum;
import org.springframework.http.HttpStatus;

/**
 * 响应策略
 *
 * @author maurice
 */
public enum ResponseStrategy implements NameValueEnum<Integer> {

    /**
     * 不支持
     */
    Unsupported(HttpStatus.HTTP_VERSION_NOT_SUPPORTED.value(), "不支持此接口"),

    /**
     * 需要更新
     */
    Upgrade(HttpStatus.UPGRADE_REQUIRED.value(), "需要更新版本");

    ResponseStrategy(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    private String name;

    private Integer value;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
