package com.example.demo.annotation;

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
     * Can be left empty if it is an Auth action.
     */
    String table() default "";

    /**
     * Determines if this is a login/logout action or a data mutation.
     */
    boolean isAuth() default false;
}
