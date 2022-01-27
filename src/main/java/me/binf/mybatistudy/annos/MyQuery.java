package me.binf.mybatistudy.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 自定义sql注解
 * @author: wangbin
 * @create: 2022/1/24 17:00
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyQuery {

    String value();
}
