package com.fuyu.basic.support.security.audit;

import org.springframework.security.core.Authentication;

import java.lang.annotation.*;

/**
 * 需审计的内容注解
 *
 * @author maurice
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * 获取当事人值的属性名称
     *
     * <p>
     * 如：
     * <p>
     * request 提交的参数或头信息存在 token=test&id=1，要获取 token 做 principal 的话，principal 就等于 token
     * <p>
     * 如果当前存在 spring security 登录用户的话。该值为{@link Authentication#getName()}
     * </p>
     *
     * @return 当事人值的属性名称
     */
    String principal() default "";

    /**
     * 审计类型
     *
     * @return 类型
     */
    String type();

}
