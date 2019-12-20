package cn.coder.struts.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Inherited
@Target(ElementType.TYPE)
@Documented
public @interface WebInitializer {

	String init() default "init";
	
	String destroy() default "destroy";
}
