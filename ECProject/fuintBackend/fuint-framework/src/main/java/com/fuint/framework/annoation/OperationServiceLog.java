package com.fuint.framework.annoation;

import java.lang.annotation.*;

/**
 * 操作日志记录注解
 *
 *
 * 
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationServiceLog {
    String description() default "";
}
