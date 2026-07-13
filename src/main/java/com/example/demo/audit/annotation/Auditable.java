package com.example.demo.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark service methods for automatic auditing.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Action type: e.g., CREATE, UPDATE, DELETE, LOGIN, LOGOUT
     */
    String action();

    /**
     * Target table or entity name: e.g., Contracts, Projects, Users
     * Có thể để trống nếu là Auth action.
     */
    String table() default "";

    /**
     * Xác định đây là hành động đăng nhập/đăng xuất hay là thay đổi dữ liệu
     */
    boolean isAuth() default false;
}
