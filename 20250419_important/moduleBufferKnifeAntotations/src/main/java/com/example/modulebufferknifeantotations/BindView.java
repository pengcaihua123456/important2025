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
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value(); // View ID
}