package com.timel.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标题：
 * 描述：
 * 作者：黄好杨
 * 创建时间：2019-12-08 13:11
 **/


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR,ElementType.METHOD})
public @interface LogTime {
}
