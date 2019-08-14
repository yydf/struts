package cn.coder.struts.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import cn.coder.struts.support.ActionSupport;

@Documented
@Retention(RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface With {

	Class<? extends ActionSupport>[] value();

}
