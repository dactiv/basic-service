package com.github.dactiv.basic.socket.client.holder.annotation;

import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;

import java.lang.annotation.*;

/**
 * socket 消息注解，用于配合 {@link com.github.dactiv.basic.socket.client.holder.SocketResultHolder} 使用，
 * 当方法使用该注解时，会根据 {@link SocketResultHolder#get()} 的值自动发送 socket 消息
 *
 * @author maurice.chen
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketMessage {

    /**
     * 属性过滤 id
     *
     * @return id
     */
    String value() default "";
}
