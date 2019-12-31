package com.highgreat.sven.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)//声明我们定义的注解的生命周期   java--->class-->run
@Target(ElementType.FIELD)
public @interface BindView {
    int value();
}
