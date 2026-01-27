package com.sinosoft.testdesign.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用日志注解
 * 用于标记需要记录应用日志的方法
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppLog {
    
    /**
     * 应用类型
     */
    String appType() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean logRequest() default true;
    
    /**
     * 是否记录响应结果
     */
    boolean logResponse() default true;
}
