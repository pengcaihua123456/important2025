package com.example.module_route.lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 必须为 RUNTIME
@Target(ElementType.METHOD)
public @interface GET {
    String value();
}