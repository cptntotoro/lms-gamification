package ru.misis.gamification.interceptor;


import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TokenCheck {
    String headerName() default "token";
}