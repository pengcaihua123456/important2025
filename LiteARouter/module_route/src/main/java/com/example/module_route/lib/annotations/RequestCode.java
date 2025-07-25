package com.example.module_route.lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Activity RequestCode Annotation
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 16/10/21 11:30]
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestCode {
    int value();
}
