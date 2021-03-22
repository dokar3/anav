package com.dokar.anav.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Navigable {

    String group() default "";

    String[] args() default {};

    Class<?>[] argTypes() default {};
}
