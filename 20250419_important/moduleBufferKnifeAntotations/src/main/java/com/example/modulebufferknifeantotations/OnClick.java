package com.example.modulebufferknifeantotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author pengcaihua
 * @Date 17:41
 * @describe
 */
// OnClick.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface OnClick {
    int value(); // View ID
}
