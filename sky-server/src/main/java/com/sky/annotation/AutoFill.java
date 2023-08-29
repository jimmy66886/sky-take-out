package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zzmr
 * @create 2023-08-28 14:32
 * 自定义注解，用于标识方法需要进行公共字段的自动填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    /**
     * 数据库操作类型 update,insert
     * @return
     */
    OperationType value();

}
