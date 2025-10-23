package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * auto fill annotation used for setting createTime, updateTime, createUser, updateUser
 */
@Target(ElementType.METHOD) // can be applied to methods
@Retention(RetentionPolicy.RUNTIME)
public @interface Autofill {
    // this is a property of the annotation, not a method
    OperationType value(); // specify the type of operation

    // we can also set other properties if needed
    // e.g., boolean logOperation() default true;
}
