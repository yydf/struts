package cn.coder.struts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;

@Retention(RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
public @interface StartUp {

}
