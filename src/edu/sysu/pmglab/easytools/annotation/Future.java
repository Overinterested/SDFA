package edu.sysu.pmglab.easytools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})

public @interface Future {
    String value() default "";

    String by() default "Wenjie Peng";

    String reason() default "";
}