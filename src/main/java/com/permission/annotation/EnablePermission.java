package com.permission.annotation;

import com.permission.config.WebConfig;
import com.permission.filter.PermissionFilter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: guozhiyang_vendor
 * @Date: 2019/5/23 10:35
 * @Version 1.0
 */
@Import(WebConfig.class)
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnablePermission {
}
