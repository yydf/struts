package cn.coder.struts.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
public @interface Request {

	String value();

	HttpMethod[] method() default HttpMethod.GET;

	public enum HttpMethod {
		GET, POST, PUT
	}

}
