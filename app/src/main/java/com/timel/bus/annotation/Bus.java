package com.timel.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标题：
 * 描述：
 * 作者：黄好杨
 * 创建时间：2019-12-09 18:24
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bus {

    int DEFAULT = -1;
    int UI = 0;
    int BG = 1;

    /**
     * 事件订阅的线程
     *
     * @return
     */
    int thread() default DEFAULT;

    /**
     * 事件id
     *
     * @return
     */
    int value();
}
