package cn.coder.struts.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Order {

	int value() default 0;
	
}
